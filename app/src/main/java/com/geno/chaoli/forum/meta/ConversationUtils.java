package com.geno.chaoli.forum.meta;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.cookie.Cookie;

/**
 * Created by jianhao on 16-3-11.
 */
public class ConversationUtils {
    /* 给主题设置版块 */
    public static final String setChannelURL = "https://chaoli.club/index.php/?p=conversation/save.json/";
    /* 发表主题 */
    public static final String postConversationURL = "https://chaoli.club/index.php/?p=conversation/start.ajax";
    /* 添加可见用户 */
    public static final String addMemberURL = "https://chaoli.club/index.php/?p=conversation/addMember.ajax/";
    /* 取消可见用户 */
    public static final String removeMemberURL = "https://chaoli.club/index.php/?p=conversation/removeMember.ajax/";
    /* 获取可见用户列表 */
    public static final String getMembersAllowedURL = "https://chaoli.club/index.php/";
    /* 隐藏主题 */
    public static final String ignoreConversationURL = "https://chaoli.club/index.php/?p=conversation/ignore.ajax/";
    /* 关注主题 */
    public static final String starConversationURL = "https://chaoli.club/index.php/?p=conversation/star.json/";

    /* 茶馆 */
    public static final int CAFF_ID = 1;
    /* 数学 */
    public static final int MATH_ID = 4;
    /* 物理 */
    public static final int PHYS_ID = 5;
    /* 化学 */
    public static final int CHEM_ID = 6;
    /* 生物 */
    public static final int BIO_ID = 7;
    /* 技术 */
    public static final int TECH_ID = 8;
    /* 公告 */
    public static final int ANNOUN_ID = 28;
    /* 申诉 */
    public static final int COURT_ID = 25;
    /* 回收站 */
    public static final int RECYCLED_ID = 27;
    /* 语言 */
    public static final int LANG_ID = 40;
    /* 社科 */
    public static final int SOCSCI_ID = 34;

    /* 返回的数据错误 */
    public static final int RETURN_ERROR = -1;
    /* 取消可见用户时没有可见用户列表没有这个用户 */
    public static final int NO_THIS_MEMBER = -2;

    private static AsyncHttpClient client = new AsyncHttpClient();

    private static ArrayList<Integer> memberList = new ArrayList<>();

    public static void setChannel(final Context context, int channel, SetChannelObserver Observer){
        setChannel(context, channel, 0, Observer);
    }

    /*  设置主题的板块
    *   conversationId为0时，会设置正在编辑、还未发出的conversation的板块
    *   addMember, removeMember也是同样*/
    public static void setChannel(final Context context, int channel, int conversationId, final SetChannelObserver Observer){
        CookieUtils.saveCookie(client, context);
        String url = setChannelURL + String.valueOf(conversationId);
        RequestParams params = new RequestParams();
        params.put("channel", channel);
        params.put("userId", LoginUtils.getUserId());
        params.put("token", LoginUtils.getToken());
        client.post(context, url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                if (response.startsWith("{\"allowedSummary\"")) {
                    Observer.onSetChannelSuccess();
                } else {
                    Observer.onSetChannelFailure(RETURN_ERROR);                 //返回数据错误
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Observer.onSetChannelFailure(statusCode);
            }
        });
    }

    /*  添加可见用户（默认任何人均可见）    */
    public static void addMember(final Context context, String member, AddMemberObserver Observer){
        addMember(context, member, 0, Observer);
    }

    public static void addMember(final Context context, String member, int conversationId, final AddMemberObserver Observer){
        CookieUtils.saveCookie(client, context);
        String url = addMemberURL + String.valueOf(conversationId);
        RequestParams params = new RequestParams();
        params.put("member", member);
        params.put("userId", LoginUtils.getUserId());
        params.put("token", LoginUtils.getToken());
        client.post(context, url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                //Log.i("addMember", response);
                String idFormat = "data-id='(\\d+)'";
                Pattern pattern = Pattern.compile(idFormat);
                Matcher matcher = pattern.matcher(response);
                //Log.i("response", String.valueOf(response.length()));
                if (matcher.find()) {
                    //Log.i("id", matcher.group(1));
                    int userIdAdded = Integer.parseInt(matcher.group(1));
                    memberList.add(userIdAdded);
                    Observer.onAddMemberSuccess();
                } else {
                    Observer.onAddMemberFailure(RETURN_ERROR);              //返回数据错误
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e("addMemberError", String.valueOf(statusCode));
            }
        });
    }

    /*  取消可见用户  */
    public static void removeMember(final Context context, int userId, RemoveMemberObserver Observer){
        removeMember(context, userId, 0, Observer);
    }

    public static void removeMember(final Context context, final int userId,
                                    int conversationId, final RemoveMemberObserver Observer){
        CookieUtils.saveCookie(client, context);
        String url = removeMemberURL + String.valueOf(conversationId);
        RequestParams params = new RequestParams();
        params.put("member", userId);
        params.put("userId", LoginUtils.getUserId());
        params.put("token", LoginUtils.getToken());
        client.post(context, url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                Log.i("removeMember", response);
                if (response.startsWith("{\"allowedSummary\"")) {
                    if (memberList.contains(Integer.valueOf(userId))) {
                        memberList.remove(Integer.valueOf(userId));
                        Observer.onRemoveMemberSuccess();
                    } else {
                        Observer.onRemoveMemberFailure(NO_THIS_MEMBER);         //无此会员
                    }
                } else {
                    Observer.onRemoveMemberFailure(RETURN_ERROR);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e("removeMemberError", String.valueOf(statusCode));
                Observer.onRemoveMemberFailure(statusCode);
            }
        });
    }

    /*  发表主题    */
    public static void postConversation(Context context, String title, String content,
                                        final PostConversationObserver Observer){
        CookieUtils.saveCookie(client, context);

        final RequestParams params = new RequestParams();
        params.put("title", title);
        params.put("content", content);
        params.put("userId", LoginUtils.getUserId());
        params.put("token", LoginUtils.getToken());
        client.post(context, postConversationURL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                //Log.i("postConversation", new String(responseBody));
                String response = new String(responseBody);
                if(response.startsWith("{\"redirect\"")){
                    String conIdFormat = "/(\\d+)";
                    Pattern pattern = Pattern.compile(conIdFormat);
                    Matcher matcher = pattern.matcher(response);
                    if(matcher.find()){
                        Observer.onPostConversationSuccess(Integer.parseInt(matcher.group(1)));
                    }else{
                        Observer.onPostConversationFailure(RETURN_ERROR);
                    }
                } else {
                    Observer.onPostConversationFailure(RETURN_ERROR);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                //Log.e("postConversationError", String.valueOf(statusCode));
                Observer.onPostConversationFailure(statusCode);
            }
        });
    }

    /*  获取可见用户列表    */
    public static void getMembersAllowed(Context context, int conId, final GetMembersAllowedObserver Observer){
        CookieUtils.saveCookie(client, context);
        final List<Integer> memberList = new ArrayList<>();
        String url = getMembersAllowedURL;
        RequestParams params = new RequestParams();
        params.put("p", "conversation/membersAllowed.ajax/" + conId);
        params.put("token", LoginUtils.getToken());
        params.put("userId", LoginUtils.getUserId());
        client.get(context, url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                Log.i("get", response);
                String idFormat = "data-id='(\\d+)'";           //按此格式从返回的数据中获取id
                Pattern pattern = Pattern.compile(idFormat);
                Matcher matcher = pattern.matcher(response);
                while (matcher.find()) {
                    memberList.add(Integer.valueOf(matcher.group(1)));
                }

                //返回的数据中，若可见用户只有自己，则返回自己的id，若有其他人，则不包含自己的id，所以要加上自己的id
                if (memberList.size() > 1 || (memberList.size() == 1 && memberList.get(0) != LoginUtils.getUserId())) {
                    memberList.add(LoginUtils.getUserId());
                }
                Observer.onGetMembersAllowedSuccess(memberList);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Observer.onGetMembersAllowedFailure(statusCode);
            }
        });
    }

    /*  隐藏/取消隐藏该主题
    *   执行操作后主题的状态为隐藏，则isIgnored为true，否则为false*/
    public static void ignoreConversation(Context context, int conversationId,
                                          final IgnoreAndStarConversationObserver observer){
        CookieUtils.saveCookie(client, context);
        String url = ignoreConversationURL + conversationId;
        RequestParams params = new RequestParams();
        params.put("userId", LoginUtils.getUserId());
        params.put("token", LoginUtils.getToken());
        client.get(context, url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                if(response.contains("\"ignored\":true")) {
                    observer.onIgnoreConversationSuccess(true);
                }else if(response.contains("\"ignored\":false")){
                    observer.onIgnoreConversationSuccess(false);
                }else{
                    Log.e("ignore", "response = " + response);
                    observer.onIgnoreConversationFailure(RETURN_ERROR);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                observer.onIgnoreConversationFailure(statusCode);
            }
        });
    }

    /*  关注/取消关注该主题
    *   执行操作后主题的状态为被关注，则isStarred为true，否则为false*/
    public static void starConversation(Context context, int conversationId,
                                        final IgnoreAndStarConversationObserver observer) {
        CookieUtils.saveCookie(client, context);
        String url = starConversationURL + conversationId;
        RequestParams params = new RequestParams();
        params.put("userId", LoginUtils.getUserId());
        params.put("token", LoginUtils.getToken());
        client.get(context, url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                if (response.contains("\"starred\":true")) {
                    observer.onStarConversationSuccess(true);
                } else if (response.contains("\"starred\":false")) {
                    observer.onStarConversationSuccess(false);
                } else {
                    observer.onStarConversationFailure(RETURN_ERROR);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                observer.onStarConversationFailure(statusCode);
            }
        });
    }

    public interface SetChannelObserver {
        void onSetChannelSuccess();
        void onSetChannelFailure(int statusCode);
    }

    public interface AddMemberObserver {
        void onAddMemberSuccess();
        void onAddMemberFailure(int statusCode);
    }

    public interface RemoveMemberObserver {
        void onRemoveMemberSuccess();
        void onRemoveMemberFailure(int statusCode);
    }

    public interface PostConversationObserver {
        void onPostConversationSuccess(int conversationId);
        void onPostConversationFailure(int statusCode);
    }

    public interface GetMembersAllowedObserver {
        void onGetMembersAllowedSuccess(List<Integer> memberList);
        void onGetMembersAllowedFailure(int statusCode);
    }

    public interface IgnoreAndStarConversationObserver{
        void onIgnoreConversationSuccess(Boolean isIgnored);
        void onIgnoreConversationFailure(int statusCode);
        void onStarConversationSuccess(Boolean isStarred);
        void onStarConversationFailure(int statusCode);
    }
}
