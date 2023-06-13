package com.yg.util;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultClientConnectionReuseStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Http请求工具类
 */
public final class HttpRequester {
    private static Logger logger = LoggerFactory.getLogger(HttpRequester.class);

    private static RestTemplate template = new RestTemplate();

    static {
        List<HttpMessageConverter<?>> converters = template.getMessageConverters();
        for (int i = 0, size = converters.size(); i < size; i++) {
            if (converters.get(i) instanceof StringHttpMessageConverter) {
                converters.remove(i);
                converters.add(i, new StringHttpMessageConverter(Charset.forName("UTF-8")));
                break;
            }
        }

        // 连接池
        HttpClient httpClient = HttpClientBuilder.create()
                .setMaxConnTotal(512)
                .setMaxConnPerRoute(512)
                //.evictIdleConnections(1200L, TimeUnit.SECONDS)
                .setConnectionReuseStrategy(DefaultClientConnectionReuseStrategy.INSTANCE)
//                .setDefaultHeaders(Collections.singleton(new KeepAliveHeader()))
                .build();

        // 连接超时
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectionRequestTimeout(2000);
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(6000);

        template.setRequestFactory(factory);
    }

    public static String get(String url) {
        return get(url, String.class);
    }

    public static <T> T get(String url, Class<T> clazz) {
        return template.getForObject(getEncodeUri(url), clazz);
    }

    public static <T> T get(String url, MultiValueMap<String, String> headers, Class<T> clazz) {
        HttpEntity<T> entity = new HttpEntity<>(headers);
        return template.exchange(url, HttpMethod.GET, entity, clazz).getBody();
    }

    /**
     * 对url进行html编码处理
     *
     * 注:
     * 调用template的方法时, 如果直接传String类型的url, spring会在内部进行编码处理, 但spring不处理";"。
     * 搜索引擎不支持";", 只支持";"的html编码"%3B"
     *
     * 如果不传";"号, 改为传"%3B", "%3B"又会被spring转义成"%253B"
     *
     * 所以此处先调用spring自身的编码方法对url进行编码, 再手动处理url,
     * 之后将编码后的url重新转成URI, 调用template的方法发送请求。
     *
     * 对于URI类型的请求, template内部不会再去编码
     *
     * @param url 请求url
     * @return 编码后的请求URI
     */
    private static URI getEncodeUri(String url) {
        // 先对url进行html编码
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(url).build().encode();
        // 由于spring不会对";"进行html编码, 所以此处需要手动替换。 考虑到调用方可能传"%3B", 需要将%重新转回来
        String encodeUrl = uriComponents.toUriString().replace(";", "%3B").replace("%25", "%");
        // 将编码后的url重新转成URI, build方法的参数传true, 表示已编码过, 无需再次编码
        return UriComponentsBuilder.fromHttpUrl(encodeUrl).build(true).toUri();
    }

    /**
     * post请求
     */
    @SuppressWarnings("unused")
    public static <T> T postForEntity(String url, Map<String, String> paramMap, Class<T> clazz) {
        ResponseEntity<T> response = template.postForEntity(getEncodeUri(url), paramMap, clazz);
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            logger.error("请求出错, 错误码: {}, 错误信息: {}", response.getStatusCode(), response.getBody());
            throw new IllegalArgumentException("请求出错");
        }
    }
}
