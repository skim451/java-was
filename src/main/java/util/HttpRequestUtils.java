package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRequestUtils {
    
    private static final Logger log = LoggerFactory.getLogger(HttpRequestUtils.class);
    
    /**
     * @param queryString은
     *            URL에서 ? 이후에 전달되는 field1=value1&field2=value2 형식임
     * @return
     */
    public static Map<String, String> parseQueryString(String queryString) {
        return parseValues(queryString, "&");
    }

    /**
     * @param 쿠키
     *            값은 name1=value1; name2=value2 형식임
     * @return
     */
    public static Map<String, String> parseCookies(String cookies) {
        return parseValues(cookies, ";");
    }

    public static String getRequestBody(List<String> request) {
        return request.get(request.size() - 1);
    }

    public static String getContentType(List<String> request) {
        String contentType = "";

        for(String line : request ){
            if(line.contains("Accept")) {
                contentType = line.split(":")[1]
                        .trim()
                        .split(",")[0];
                break;
            }
        }

        return contentType;
    }

    public static boolean getLoginStatus(List<String> request) throws IOException {
        String loginStatus = "";

        for(String line : request ){
            if(line.contains("Cookie")) {
                 loginStatus = line.split(":")[1].split("=")[1];
                 break;
            }
        }

        return loginStatus.equals("true");
    }

    private static Map<String, String> parseValues(String values, String separator) {
        if (Strings.isNullOrEmpty(values)) {
            return Maps.newHashMap();
        }

        String[] tokens = values.split(separator);
        return Arrays.stream(tokens)
                .map(t -> getKeyValue(t, "="))
                .filter(p -> p != null)
                .collect(Collectors
                        .toMap(p -> p.getKey(), p -> p.getValue()));
    }

    static Pair getKeyValue(String keyValue, String regex) {
        if (Strings.isNullOrEmpty(keyValue)) {
            return null;
        }

        String[] tokens = keyValue.split(regex);
        if (tokens.length != 2) {
            return null;
        }

        return new Pair(tokens[0], tokens[1]);
    }

    public static Pair parseHeader(String header) {
        return getKeyValue(header, ": ");
    }

    public static byte[] getResource(String resource) throws IOException {
        return Files.readAllBytes(new File("./webapp" + resource).toPath());
    }

    public static List<String> readWholeRequest(BufferedReader br) throws IOException {
        List<String> lineList = new ArrayList<>();
        String line;
        int contentLength = 0;
        do {
            line = br.readLine();
            lineList.add(line);
            if (line.contains("Content-Length")) {
                contentLength = Integer.parseInt(line.split(":")[1].trim());
            }
        } while(!line.equals(""));

        lineList.add(IOUtils.readData(br, contentLength));
        return lineList;
    }

    public static byte[] getUserListBody(Collection<User> users) {
        StringBuilder builder = new StringBuilder();
        users.forEach(user -> builder.append(user.toString()));

        return builder.toString().getBytes();
    }

    public static class Pair {
        String key;
        String value;

        Pair(String key, String value) {
            this.key = key.trim();
            this.value = value.trim();
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Pair other = (Pair) obj;
            if (key == null) {
                if (other.key != null)
                    return false;
            } else if (!key.equals(other.key))
                return false;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "Pair [key=" + key + ", value=" + value + "]";
        }
    }
}
