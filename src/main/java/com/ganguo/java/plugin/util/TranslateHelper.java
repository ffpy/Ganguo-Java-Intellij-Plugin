package com.ganguo.java.plugin.util;

import com.ganguo.java.plugin.service.SettingService;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 翻译工具类
 */
public class TranslateHelper {
    private static final String URL = "http://api.fanyi.baidu.com/api/trans/vip/translate";
    private static final int MAX_TEXT_BYTES_LENGTH = 6000;

    private final String appId;
    private final String secret;

    public TranslateHelper(String appId, String secret) {
        this.appId = appId;
        this.secret = secret;
        if (StringUtils.isEmpty(appId) || StringUtils.isEmpty(secret)) {
            throw new RuntimeException("百度翻译应用ID和密钥不能为空");
        }
    }

    public TranslateHelper(Project project) {
        SettingService settingService = ServiceManager.getService(project, SettingService.class);
        appId = settingService.getTranslateAppId();
        secret = settingService.getTranslateSecret();
        if (StringUtils.isEmpty(appId) || StringUtils.isEmpty(secret)) {
            throw new RuntimeException("百度翻译应用ID和密钥不能为空");
        }
    }

    /**
     * 中文转英文
     *
     * @param text 中午字符串
     * @return 英文字符串
     */
    public String zh2En(String text) throws Exception {
        return translate(text, "zh", "en");
    }

    /**
     * 翻译
     * 语言列表参考http://api.fanyi.baidu.com/api/trans/product/apidoc
     *
     * @param text 待翻译文本(UTF-8编码)
     * @param from 源语言，auto为自动检测
     * @param to   目标语言
     * @return 结果文本
     */
    public String translate(String text, String from, String to) throws Exception {
        if (StringUtils.isEmpty(text)) {
            return text;
        }
        if (text.getBytes().length > MAX_TEXT_BYTES_LENGTH) {
            throw new IllegalArgumentException("文本不能超过6000个字节");
        }
        String salt = getSalt();
        Map<String, String> params = new HashMap<>();
        params.put("q", text);
        params.put("from", Objects.requireNonNull(from));
        params.put("to", Objects.requireNonNull(to));
        params.put("appid", appId);
        params.put("salt", salt);
        params.put("sign", getSign(text, salt));

        HttpClientUtils.HttpClientResult response = HttpClientUtils.doGet(URL, params);
        if (response.getCode() == HttpStatus.SC_OK) {
            return new JSONObject(response.getContent()).getJSONArray("trans_result")
                    .getJSONObject(0).getString("dst");
        }
        throw new Exception("翻译失败 response: " + response);
    }

    private String getSign(String text, String salt) {
        return DigestUtils.md5Hex(appId + text + salt + secret);
    }

    private String getSalt() {
        return String.valueOf(System.currentTimeMillis());
    }
}
