package com.entry_level_jobs.service;

import com.entry_level_jobs.model.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Service for fetching job listings from various sources.
 * Currently integrated with Arbeitnow public API.
 */
@Service
@Slf4j
public class JobFetchService {
    private final ArbeitnowJobFetchService arbeitnowService;
    private final RemotiveJobFetchService remotiveService;
    private final MuseJobFetchService museService;

    @Autowired
    public JobFetchService(ArbeitnowJobFetchService arbeitnowService,
            RemotiveJobFetchService remotiveService,
            MuseJobFetchService museService) {
        this.arbeitnowService = arbeitnowService;
        this.remotiveService = remotiveService;
        this.museService = museService;
    }

    /**
     * Fetch jobs from external APIs.
     * Retrieves jobs from Arbeitnow API (pages 1-3 to get a good variety).
     * Also fetches from Remotive and The Muse.
     *
     * @return List of fetched jobs from all sources (deduplicated)
     */
    public List<Job> fetchJobsFromApis() {
        log.info("Fetching jobs from external APIs");
        Map<String, Job> uniqueByKey = new LinkedHashMap<>();

        // Fetch from Arbeitnow API (pages 1-3)
        for (int page = 1; page <= 3; page++) {
            List<Job> pageJobs = arbeitnowService.fetchJobsFromArbeitnow(page);
            for (Job j : pageJobs)
                addIfUnique(uniqueByKey, j);

            if (pageJobs.isEmpty()) {
                log.info("No more jobs available from Arbeitnow on page {}", page);
                break;
            }
        }

        // Fetch from Remotive
        try {
            List<Job> remotiveJobs = remotiveService.fetchJobsFromRemotive();
            for (Job j : remotiveJobs)
                addIfUnique(uniqueByKey, j);
        } catch (Exception e) {
            log.error("Error fetching jobs from Remotive", e);
        }

        // Fetch from The Muse
        try {
            List<Job> museJobs = museService.fetchJobsFromMuse();
            for (Job j : museJobs)
                addIfUnique(uniqueByKey, j);
        } catch (Exception e) {
            log.error("Error fetching jobs from The Muse", e);
        }

        List<Job> allJobs = new ArrayList<>(uniqueByKey.values());
        sortByRecency(allJobs);
        log.info("Successfully fetched {} total unique jobs from external APIs", allJobs.size());
        return allJobs;
    }

    private void sortByRecency(List<Job> jobs) {
        Comparator<Job> comparator = Comparator
                .comparing(Job::getPostedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Job::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Job::getId, Comparator.nullsLast(Comparator.reverseOrder()));
        jobs.sort(comparator);
    }

    private void addIfUnique(Map<String, Job> map, Job job) {
        String key = normalizedKeyForJob(job);
        if (!map.containsKey(key)) {
            map.put(key, job);
        } else {
            // Merge: keep existing or prefer earlier postedAt
            Job existing = map.get(key);
            if (job.getPostedAt() != null && existing.getPostedAt() != null
                    && job.getPostedAt().isBefore(existing.getPostedAt())) {
                map.put(key, job);
            }
        }
    }

    private String normalizedKeyForJob(Job job) {
        if (job == null)
            return "";
        String url = job.getUrl();
        if (url != null && !url.isBlank()) {
            String norm = normalizeUrl(url);
            if (norm != null && !norm.isBlank())
                return norm;
        }
        return fingerprint(job);
    }

    private String normalizeUrl(String url) {
        if (url == null)
            return "";
        String raw = url.trim();
        try {
            // Parse URI; if host missing, try adding http scheme
            URI uri = new URI(raw);
            if (uri.getHost() == null) {
                uri = new URI("http://" + raw);
            }

            String scheme = uri.getScheme() != null ? uri.getScheme().toLowerCase() : "http";
            String host = uri.getHost() != null ? uri.getHost().toLowerCase() : "";
            // drop www prefix
            if (host.startsWith("www."))
                host = host.substring(4);

            int port = uri.getPort();
            boolean includePort = port != -1
                    && !(("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443));

            String rawPath = uri.getRawPath();
            if (rawPath == null || rawPath.isEmpty())
                rawPath = "/";
            String path = canonicalizePath(rawPath);
            // remove trailing slash unless it's the root
            if (path.endsWith("/") && path.length() > 1) {
                path = path.substring(0, path.length() - 1);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(scheme).append("://").append(host);
            if (includePort)
                sb.append(":" + port);
            sb.append(path);
            return sb.toString();
        } catch (Exception e) {
            // Fallback: best-effort cleanup similar to prior implementation but more robust
            String s = raw;
            int q = s.indexOf('?');
            if (q > -1)
                s = s.substring(0, q);
            int f = s.indexOf('#');
            if (f > -1)
                s = s.substring(0, f);
            s = s.trim();
            // Ensure scheme for parsing
            if (!s.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*")) {
                s = "http://" + s;
            }
            try {
                URI uri = new URI(s);
                String host = uri.getHost() != null ? uri.getHost().toLowerCase() : "";
                if (host.startsWith("www."))
                    host = host.substring(4);
                String rawPath = uri.getRawPath() == null ? "/" : uri.getRawPath();
                String path = canonicalizePath(rawPath);
                if (path.endsWith("/") && path.length() > 1)
                    path = path.substring(0, path.length() - 1);
                int port = uri.getPort();
                boolean includePort = port != -1 && !(("http".equals(uri.getScheme()) && port == 80)
                        || ("https".equals(uri.getScheme()) && port == 443));
                StringBuilder sb = new StringBuilder();
                sb.append(uri.getScheme().toLowerCase()).append("://").append(host);
                if (includePort)
                    sb.append(":" + port);
                sb.append(path);
                return sb.toString();
            } catch (Exception ex) {
                s = s.toLowerCase();
                if (s.startsWith("http://www."))
                    s = "http://" + s.substring(11);
                if (s.startsWith("https://www."))
                    s = "https://" + s.substring(12);
                int q2 = s.indexOf('?');
                if (q2 > -1)
                    s = s.substring(0, q2);
                int f2 = s.indexOf('#');
                if (f2 > -1)
                    s = s.substring(0, f2);
                if (s.endsWith("/") && s.length() > 1)
                    s = s.substring(0, s.length() - 1);
                return s;
            }
        }
    }

    // Canonicalize raw path: decode percent-encodings, lowercase, then re-encode
    // reserved characters
    private static String canonicalizePath(String rawPath) {
        if (rawPath == null || rawPath.isEmpty())
            return "/";
        String[] parts = rawPath.split("/", -1);
        StringBuilder sb = new StringBuilder();
        // preserve leading slash
        boolean leading = rawPath.startsWith("/");
        if (leading)
            sb.append('/');
        boolean firstSegment = true;
        for (int i = 0; i < parts.length; i++) {
            String seg = parts[i];
            if (seg.isEmpty()) {
                // skip empty segments (this collapses multiple slashes)
                continue;
            }
            if (!firstSegment && sb.length() > 0 && sb.charAt(sb.length() - 1) != '/')
                sb.append('/');
            String decoded = percentDecode(seg);
            // lowercase path as requested
            decoded = decoded.toLowerCase();
            String re = percentEncode(decoded);
            sb.append(re);
            firstSegment = false;
        }
        String res = sb.toString();
        if (res.isEmpty())
            res = "/";
        // ensure starts with '/'
        if (!res.startsWith("/"))
            res = "/" + res;
        return res;
    }

    private static String percentDecode(String s) {
        if (s == null || s.isEmpty())
            return "";
        ByteArrayOutputStream baos = new ByteArrayOutputStream(s.length());
        for (int i = 0; i < s.length();) {
            char c = s.charAt(i);
            if (c == '%' && i + 2 < s.length()) {
                String hex = s.substring(i + 1, i + 3);
                try {
                    int val = Integer.parseInt(hex, 16);
                    baos.write(val);
                    i += 3;
                    continue;
                } catch (NumberFormatException ignored) {
                    // fallthrough to treat '%' as literal
                }
            }
            // write the UTF-8 bytes for this character
            byte[] bytes = String.valueOf(c).getBytes(StandardCharsets.UTF_8);
            try {
                baos.write(bytes);
            } catch (Exception ignored) {
            }
            i++;
        }
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    private static boolean isUnreserved(char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9')
                || ch == '-' || ch == '.' || ch == '_' || ch == '~';
    }

    private static String percentEncode(String s) {
        if (s == null || s.isEmpty())
            return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            int codePoint = s.codePointAt(i);
            char ch = s.charAt(i);
            if (isUnreserved(ch)) {
                sb.append(ch);
            } else {
                String cp = new String(Character.toChars(codePoint));
                byte[] bytes = cp.getBytes(StandardCharsets.UTF_8);
                for (byte b : bytes) {
                    sb.append('%');
                    sb.append(String.format("%02X", b));
                }
                if (Character.isSupplementaryCodePoint(codePoint))
                    i++; // skip low surrogate
            }
        }
        return sb.toString();
    }

    private String fingerprint(Job job) {
        String base = (job.getTitle() == null ? "" : job.getTitle().toLowerCase().trim()) + "|"
                + (job.getCompany() == null ? "" : job.getCompany().toLowerCase().trim()) + "|"
                + (job.getLocation() == null ? "" : job.getLocation().toLowerCase().trim());
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(base.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(base.hashCode());
        }
    }
}
