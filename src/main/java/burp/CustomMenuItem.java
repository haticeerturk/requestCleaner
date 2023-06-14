package burp;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.Toolkit;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.swing.JMenuItem;


class CustomMenuItem implements IContextMenuFactory {
    private final IBurpExtenderCallbacks callbacks;
    private final IExtensionHelpers helpers;
    private IContextMenuInvocation invocation;

    public CustomMenuItem(IBurpExtenderCallbacks callbacks) {
        this.callbacks = callbacks;
        this.helpers = callbacks.getHelpers();
    }

    public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation) {
        this.invocation = invocation;

        // Get information from the invocation
        IHttpRequestResponse[] ihrrs = invocation.getSelectedMessages();

        JMenuItem item = new JMenuItem("Copy request without cookies/tokens");
        item.addActionListener(new CustomMenuItemListener(ihrrs, callbacks));

        List<JMenuItem> menuItems = new ArrayList<>();
        menuItems.add(item);

        return menuItems;
    }
}

class CustomMenuItemListener implements ActionListener {
    private final IBurpExtenderCallbacks callbacks;
    private final IExtensionHelpers helpers;
    private IHttpRequestResponse[] ihrrs;

    private String content = "{{ CHANGE WITH YOUR %s }}";
    private byte[] supportedParameterTypes = {IParameter.PARAM_URL, IParameter.PARAM_BODY, IParameter.PARAM_JSON,
                                            IParameter.PARAM_MULTIPART_ATTR, IParameter.PARAM_XML,
                                            IParameter.PARAM_XML_ATTR};

    public CustomMenuItemListener(IHttpRequestResponse[] ihrrs, IBurpExtenderCallbacks callbacks) {
        this.callbacks = callbacks;
        this.helpers = callbacks.getHelpers();
        this.ihrrs = ihrrs;
    }

    public void actionPerformed(ActionEvent e) {
        for (IHttpRequestResponse requestResponse : ihrrs) {
            byte[] request = requestResponse.getRequest();
            IRequestInfo requestInfo = helpers.analyzeRequest(request);
            List<IParameter> parameters = requestInfo.getParameters();

            for (IParameter param : parameters) {
                byte parameterType = param.getType();
                String parameterName = param.getName();

                if (!isSupportedType(param)) continue;

                boolean isParamExist = checkParameter(parameterName);
                if (!isParamExist) continue;

                // Change the content of URL and body parameters
                String newContentOfTheParameter = String.format(content, parameterName.toUpperCase());
                String encodedNewContent = helpers.urlEncode(newContentOfTheParameter);

                if (parameterType == IParameter.PARAM_JSON || parameterType == IParameter.PARAM_XML) {
                    request = replaceParam(request, param, encodedNewContent.getBytes());
                } else {
                    request = helpers.removeParameter(request, param);

                    IParameter newParameter = helpers.buildParameter(parameterName, encodedNewContent, param.getType());

                    request = helpers.addParameter(request, newParameter);
                }
            }

            List<String> headers = helpers.analyzeRequest(request).getHeaders();
            // Modify the content of the headers
            List<String> newHeaders = changeContentOfTheHeaders(headers);

            // Create a new modified request
            byte[] body = Arrays.copyOfRange(request, helpers.analyzeRequest(request).getBodyOffset(), request.length);
            byte[] newHttpRequest = helpers.buildHttpMessage(newHeaders, body);

            String stringHttpRequest = new String(newHttpRequest, StandardCharsets.UTF_8);

            copyToClipboard(stringHttpRequest);
        }
    }

    public boolean checkParameter(String parameterName) {
        List<String> sessionParameters = new ArrayList<>(Arrays.asList("csrf_token", "csrf", "_csrf", "authenticity_token",
                "csrfmiddlewaretoken", "X-CSRF-Token", "token", "anti_csrf_token", "security_token",
                "__RequestVerificationToken", "xsrf_token", "request_token", "csrf_nonce", "__csrf_token", "verify",
                "csrf_protect", "csrfField", "form_token", "forgery_token", "crumb", "nonce", "requestVerificationToken",
                "state", "xsrfToken", "secure_token", "authenticityToken", "csrf_key", "_token", "_xsrf",
                "anti-forgery-token", "guard", "csrf_param", "csrf_token_name", "csrf_token_value", "token_id",
                "csrfField_name", "csrf_magic", "token_key", "form_key", "secureToken", "csrfTokenName", "csrfSecret",
                "csrf_param_name", "csrf-token", "_requesttoken", "csrftoken_value", "csrf_value", "csrfKey", "csrfNonce",
                "session_id", "sessionid", "session_key", "sessiontoken", "session_token", "sid", "PHPSESSID", "JSESSIONID",
                "ASP.NET_SessionId", "auth_token", "access_token", "user_token", "user_session", "session", "login_token",
                "session_val", "s_key", "auth_session", "user_session_id", "usr_session", "session_cookie", "sessionID",
                "sessionValue", "user_auth", "auth_key", "sessionData", "remember_token", "jwt_token", "oauth_token",
                "id_token", "bearer_token", "sessionKey", "sessionString", "sessionCode", "sessionIdToken", "login_session",
                "user_session_key", "session_code", "authSessionKey"));

        Integer indexOfExistingParameter = sessionParameters.indexOf(parameterName);

        return indexOfExistingParameter >= 0 ? true : false;
    }

    public List<String> changeContentOfTheHeaders(List<String> headers) {
        List<String> authHeaders = new ArrayList<>(Arrays.asList("Authorization", "Cookie", "X-Xsrf-Token",
                "X-CSRF-Token", "X-Auth-Token", "X-User-Token", "X-Token-Auth", "X-JWT-Token", "X-Access-Token",
                "X-Refresh-Token", "X-Firebase-Auth", "X-API-Token", "X-Session-Token", "X-App-Token", "X-OAuth-Token",
                "X-Device-Token", "X-Device-Id", "X-Device-Info", "X-Device-Auth", "X-Device-Secret", "X-User-Id",
                "X-User-Auth", "X-User-Secret", "X-User-Session", "X-User-Profile-Token", "X-User-Access-Token",
                "X-User-Refresh-Token", "X-User-JWT", "X-User-Api-Key", "X-User-OAuth-Token", "X-User-Firebase-Auth",
                "X-Session-Id", "X-Session-Auth", "X-Session-Key", "X-Session-Secret", "X-Session-Data", "X-Session-Info",
                "X-Session-Attributes", "X-Session-Context", "X-Client-Token", "X-Client-Id", "X-Client-Secret",
                "X-Client-Auth", "X-Client-Session", "X-Client-Info", "X-App-Id", "X-App-Key", "X-App-Secret",
                "X-App-Auth", "X-App-Session", "X-Api-Key", "X-Api-Secret", "X-Api-Token", "X-Api-Auth",
                "X-Api-Session", "X-JWT", "X-JWT-Auth", "X-OAuth", "X-OAuth-Auth", "X-Csrf-Token",
                "X-Forwarded-Access-Token", "X-Anonymous-Consumer-Token"));

        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);

            String[] splittedHeaderList = header.split(":");
            if (splittedHeaderList.length <= 1) continue;

            String splittedHeader = splittedHeaderList[0];

            Integer indexOfExistingHeader = authHeaders.indexOf(splittedHeader);
            if (indexOfExistingHeader < 0) continue;

            String existingHeader = authHeaders.get(indexOfExistingHeader);

            String newContentOfTheHeader = String.format(content, existingHeader.toUpperCase());

            header = String.format("%s: %s", existingHeader, newContentOfTheHeader);
            headers.set(i, header);
        }

        return headers;
    }

    public void copyToClipboard(String content) {
        StringSelection stringSelection = new StringSelection(content);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    public boolean isSupportedType(IParameter param){
        byte type = param.getType();
        for (byte supported : supportedParameterTypes) {
            if (supported == type) {
                return true;
            }
        }
        return false;
    }

    public byte[] replaceParam(byte[] request, IParameter param, byte[] newValue) {
        int length = request.length;
        int start = param.getValueStart();
        int end = param.getValueEnd();

        byte[] prefix = Arrays.copyOfRange(request, 0, start);
        byte[] rest = Arrays.copyOfRange(request, end, length);

        byte[] newRequest = new byte[prefix.length + newValue.length + rest.length];
        System.arraycopy(prefix, 0, newRequest, 0, prefix.length);
        System.arraycopy(newValue, 0, newRequest, prefix.length, newValue.length);
        System.arraycopy(rest, 0, newRequest, prefix.length + newValue.length, rest.length);

        return newRequest;
    }
}