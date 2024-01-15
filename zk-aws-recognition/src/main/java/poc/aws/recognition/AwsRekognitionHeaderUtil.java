package poc.aws.recognition;


import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * AwsRekognitionHeaderUtil
 *
 * @author kangzijian
 * @date 2023/12/27 14:27
 */
public class AwsRekognitionHeaderUtil {

    static final String SCHEME = "AWS4";
    static final String ALGORITHM = "HMAC-SHA256";
    static final String TERMINATOR = "aws4_request";
    // 可以在 EC2 上获取临时 token
    // TOKEN=`curl -X PUT "http://169.254.169.254/latest/api/token" -H "X-aws-ec2-metadata-token-ttl-seconds: 21600"` && curl -H "X-aws-ec2-metadata-token: $TOKEN" -v http://169.254.169.254/latest/meta-data/iam/security-credentials/faceLivenessBackend

    static final String ACCESS_KEY_ID = CredentialConfig.AccessKey;
    static final String SECRET_ACCESS_KEY = CredentialConfig.SecretKey;
    static final String REGION_NAME = "us-west-2";
    static final String ENDPOINT = "https://rekognition.us-west-2.amazonaws.com";
    static final String HTTP_METHOD = "POST";
    static final String SERVICE_NAME = "rekognition";


    public static HttpHeaders buildGetSessionIdHeaders(String target, String objectContent) {
        if (StringUtils.isBlank(target)) {
            throw new RuntimeException();
//            throw new CommonErrorCodeException(ErrorEnum.TARGET_HEADER_NOT_BLANK);
        }
        HttpHeaders headers = new HttpHeaders();
        // 0.0 准备⼯作
        URL endpointUrl;
        try {
            endpointUrl = new URL(ENDPOINT);
        } catch (MalformedURLException e) {
            throw new RuntimeException();
//            log.error("AwsRekognitionHeaderUtil.buildHeader错误：", e);
//            throw new BusinessException("AwsRekognitionHeaderUtil.buildHeader报错");
        }
        byte[] contentHash = hash(objectContent);
        String contentHashString = toHex(contentHash);
        headers.add("Content-Type", "application/x-amz-json-1.1");
        headers.add("X-Amz-Target", target);
        headers.add("X-Amz-Content-Sha256", contentHashString);
        Map<String, String> queryParams = new HashMap<>();
        String authorization = computeSignature(endpointUrl, headers, queryParams, contentHashString, ACCESS_KEY_ID, SECRET_ACCESS_KEY, SERVICE_NAME);
        headers.add("Authorization", authorization);
        return headers;
    }

    public static HttpHeaders buildGetSTSHeaders(String target, String objectContent) {
        if (StringUtils.isBlank(target)) {
            throw new RuntimeException();
//            throw new CommonErrorCodeException(ErrorEnum.TARGET_HEADER_NOT_BLANK);
        }
        HttpHeaders headers = new HttpHeaders();
        // 0.0 准备⼯作
        URL endpointUrl;
        try {
            endpointUrl = new URL(ENDPOINT);
        } catch (MalformedURLException e) {
            throw new RuntimeException();
//            log.error("AwsRekognitionHeaderUtil.buildHeader错误：", e);
//            throw new BusinessException("AwsRekognitionHeaderUtil.buildHeader报错");
        }
        byte[] contentHash = hash(objectContent);
        String contentHashString = toHex(contentHash);
        headers.add("Content-Type", "application/x-amz-json-1.1");
        headers.add("X-Amz-Target", target);
        headers.add("X-Amz-Content-Sha256", contentHashString);
        Map<String, String> queryParams = new HashMap<>();
        String authorization = computeSignature(endpointUrl, headers, queryParams, contentHashString, ACCESS_KEY_ID, SECRET_ACCESS_KEY, "sts");
        headers.add("Authorization", authorization);
        return headers;
    }

    protected static String getCanonicalRequest(URL endpoint, String httpMethod, String queryParameters, String canonicalizeHeaderNames, String canonicalizeHeaders, String bodyHash) {
        return httpMethod + "\n" + getCanonicalizeResourcePath(endpoint) + "\n" + queryParameters + "\n" + canonicalizeHeaders + "\n" + canonicalizeHeaderNames + "\n" + bodyHash;
    }

    protected static String getCanonicalizeResourcePath(URL endpoint) {
        if (endpoint == null) {
            return "/";
        }
        String path = endpoint.getPath();
        if (path == null || path.isEmpty()) {
            return "/";
        }
        String encodedPath = urlEncode(path, true);
        if (encodedPath.startsWith("/")) {
            return encodedPath;
        } else {
            return "/".concat(encodedPath);
        }
    }

    public static String urlEncode(String url, boolean keepPathSlash) {
        String encoded;
        try {
            encoded = URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException();
//            throw new BusinessException("UTF-8 encoding is not supported.", e);
        }
        if (keepPathSlash) {
            encoded = encoded.replace("%2F", "/");
        }
        return encoded;
    }

    public static byte[] hash(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(text.getBytes(StandardCharsets.UTF_8));
            return md.digest();
        } catch (Exception e) {
            throw new RuntimeException();
//            throw new BusinessException("Unable to compute hash while signing request: " + e.getMessage(), e);
        }
    }

    public static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (int i = 0; i < data.length; i++) {
            String hex = Integer.toHexString(data[i]);
            if (hex.length() == 1) {
                // Append leading zero.
                sb.append("0");
            } else if (hex.length() == 8) {
                // Remove ff prefix from negative numbers.
                hex = hex.substring(6);
            }
            sb.append(hex);
        }
        return sb.toString().toLowerCase(Locale.getDefault());
    }

    public static String invokeHttpRequest(URL endpointUrl, String httpMethod, Map<String, String> headers, String requestBody) {
        HttpURLConnection connection = createHttpConnection(endpointUrl, httpMethod, headers);
        try {
            if (requestBody != null) {
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(requestBody);
                wr.flush();
                wr.close();
            }
        } catch (Exception e) {
            throw new RuntimeException();
//            throw new BusinessException("Request failed. " + e.getMessage(), e);
        }
        return executeHttpRequest(connection);
    }

    public static HttpURLConnection createHttpConnection(URL endpointUrl, String httpMethod, Map<String, String> headers) {
        try {
            HttpURLConnection connection = (HttpURLConnection) endpointUrl.openConnection();
            connection.setRequestMethod(httpMethod);
            if (headers != null) {
                System.out.println("--------- Request headers ---------");
                for (String headerKey : headers.keySet()) {
                    System.out.println(headerKey + ": " + headers.get(headerKey));
                    connection.setRequestProperty(headerKey, headers.get(headerKey));
                }
            }
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            return connection;
        } catch (Exception e) {
            throw new RuntimeException();
//            throw new BusinessException("Cannot create connection. " + e.getMessage(), e);
        }
    }

    public static String executeHttpRequest(HttpURLConnection connection) {
        try {
            // Get Response
            InputStream is;
            try {
                is = connection.getInputStream();
            } catch (IOException e) {
                is = connection.getErrorStream();
            }
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            throw new RuntimeException();
//            throw new BusinessException("Request failed. " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static String computeSignature(URL endpointUrl, HttpHeaders headers, Map<String, String> queryParameters, String bodyHash, String accessKeyId, String secretAccessKey, String serviceName) {
        Date now = new Date();
        final SimpleDateFormat dateTimeFormat;
        final SimpleDateFormat dateStampFormat;
        final String ISO8601BasicFormat = "yyyyMMdd'T'HHmmss'Z'";
        final String DateStringFormat = "yyyyMMdd";
        dateTimeFormat = new SimpleDateFormat(ISO8601BasicFormat);
        dateTimeFormat.setTimeZone(new SimpleTimeZone(0, "UTC"));
        dateStampFormat = new SimpleDateFormat(DateStringFormat);
        dateStampFormat.setTimeZone(new SimpleTimeZone(0, "UTC"));
        String dateTimeStamp = dateTimeFormat.format(now);
        headers.add("X-Amz-Date", dateTimeStamp);
        String hostHeader = endpointUrl.getHost();
        int port = endpointUrl.getPort();
        if (port > -1) {
            hostHeader = hostHeader.concat(":" + port);
        }
        headers.add("Host", hostHeader);
        String canonicalizeHeaderNames = getCanonicalizeHeaderNames(headers);
        String canonicalizeHeaders = getCanonicalizeHeaderString(headers);
        String canonicalizeQueryParameters = getCanonicalizeQueryString(queryParameters);
        String canonicalRequest = getCanonicalRequest(endpointUrl, HTTP_METHOD, canonicalizeQueryParameters, canonicalizeHeaderNames, canonicalizeHeaders, bodyHash);
        String dateStamp = dateStampFormat.format(now);
        String scope = dateStamp + "/" + REGION_NAME + "/" + serviceName + "/" + TERMINATOR;
        String stringToSign = getStringToSign(SCHEME, ALGORITHM, dateTimeStamp, scope, canonicalRequest);
        byte[] kSecret = (SCHEME + secretAccessKey).getBytes();
        byte[] kDate = sign(dateStamp, kSecret, "HmacSHA256");
        byte[] kRegion = sign(REGION_NAME, kDate, "HmacSHA256");
        byte[] kService = sign(SERVICE_NAME, kRegion, "HmacSHA256");
        byte[] kSigning = sign(TERMINATOR, kService, "HmacSHA256");
        byte[] signature = sign(stringToSign, kSigning, "HmacSHA256");
        String credentialsAuthorizationHeader = "Credential=" + accessKeyId + "/" + scope;
        String signedHeadersAuthorizationHeader = "SignedHeaders=" + canonicalizeHeaderNames;
        String signatureAuthorizationHeader = "Signature=" + toHex(signature);
        return SCHEME + "-" + ALGORITHM + " " + credentialsAuthorizationHeader + ", " + signedHeadersAuthorizationHeader + ", " + signatureAuthorizationHeader;
    }

    protected static byte[] sign(String stringData, byte[] key, String algorithm) {
        try {
            byte[] data = stringData.getBytes("UTF-8");
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(key, algorithm));
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException();
//            throw new BusinessException("Unable to calculate a request signature: " + e.getMessage(), e);
        }
    }

    protected static String getStringToSign(String scheme, String algorithm, String dateTime, String scope, String canonicalRequest) {
        return scheme + "-" + algorithm + "\n" + dateTime + "\n" + scope + "\n" + toHex(hash(canonicalRequest));
    }

    public static String getCanonicalizeQueryString(Map<String, String> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return "";
        }
        SortedMap<String, String> sorted = new TreeMap<>();
        Iterator<Map.Entry<String, String>> pairs = parameters.entrySet().iterator();
        while (pairs.hasNext()) {
            Map.Entry<String, String> pair = pairs.next();
            String key = pair.getKey();
            String value = pair.getValue();
            sorted.put(urlEncode(key, false), urlEncode(value, false));
        }
        StringBuilder builder = new StringBuilder();
        pairs = sorted.entrySet().iterator();
        while (pairs.hasNext()) {
            Map.Entry<String, String> pair = pairs.next();
            builder.append(pair.getKey());
            builder.append("=");
            builder.append(pair.getValue());
            if (pairs.hasNext()) {
                builder.append("&");
            }
        }
        return builder.toString();
    }

    protected static String getCanonicalizeHeaderString(HttpHeaders headers) {
        if (headers == null || headers.isEmpty()) {
            return "";
        }

        // 步骤1：按不区分大小写的顺序对标头进行排序
        List<String> sortedHeaders = new ArrayList<>(headers.keySet());
        sortedHeaders.sort(String.CASE_INSENSITIVE_ORDER);
        // 步骤2：形成规范请求头：按排序顺序的值项。
        // 值中的多个空格应压缩为单个空格。
        StringBuilder builder = new StringBuilder();
        for (String key : sortedHeaders) {
            builder.append(key.toLowerCase().replaceAll("\\s+", " "))
                    .append(":")
                    .append(headers.get(key)
                            .get(0).replaceAll("\\s+", " "));
            builder.append("\n");
        }
        return builder.toString();
    }

    protected static String getCanonicalizeHeaderNames(HttpHeaders headers) {
        List<String> sortedHeaders = new ArrayList<>(headers.keySet());
        sortedHeaders.sort(String.CASE_INSENSITIVE_ORDER);
        StringBuilder buffer = new StringBuilder();
        for (String header : sortedHeaders) {
            if (buffer.length() > 0) {
                buffer.append(";");
            }
            buffer.append(header.toLowerCase());
        }
        return buffer.toString();
    }

}
