package com.lauzzl.nowatermark.factory.parser;

import cn.hutool.core.annotation.Alias;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dtflys.forest.Forest;
import com.dtflys.forest.http.ForestCookie;
import com.dtflys.forest.http.ForestProxy;
import com.lauzzl.nowatermark.base.code.ErrorCode;
import com.lauzzl.nowatermark.base.config.ProxyConfig;
import com.lauzzl.nowatermark.base.domain.Result;
import com.lauzzl.nowatermark.factory.enums.MediaTypeEnum;
import com.lauzzl.nowatermark.base.enums.UserAgentPlatformEnum;
import com.lauzzl.nowatermark.base.model.resp.ParserResp;
import com.lauzzl.nowatermark.base.utils.CommonUtil;
import com.lauzzl.nowatermark.base.utils.ParserResultUtils;
import com.lauzzl.nowatermark.base.utils.UrlUtil;
import com.lauzzl.nowatermark.factory.Parser;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class Twitter implements Parser {

    @Resource
    private ProxyConfig proxyConfig;

    @Value("${account.twitter.auth_token}")
    private String authToken;

    @Value("${account.twitter.x_csrf_token}")
    private String xCsrfToken;

    @Value("${account.twitter.authorization}")
    private String Authorization;


    // graphql.queryId -> https://abs.twimg.com/responsive-web/client-web/main.8e00a9aa.js
    //    """
    //    e.exports = {
    //            queryId: "iFEr5AcP121Og4wx9Yqo3w",
    //            operationName: "TweetDetail",
    //            operationType: "query",
    //            metadata: {
    //                featureSwitches: ["rweb_video_screen_enabled", "payments_enabled", "rweb_xchat_enabled", "profile_label_improvements_pcf_label_in_post_enabled", "rweb_tipjar_consumption_enabled", "verified_phone_label_enabled", "creator_subscriptions_tweet_preview_api_enabled", "responsive_web_graphql_timeline_navigation_enabled", "responsive_web_graphql_skip_user_profile_image_extensions_enabled", "premium_content_api_read_enabled", "communities_web_enable_tweet_community_results_fetch", "c9s_tweet_anatomy_moderator_badge_enabled", "responsive_web_grok_analyze_button_fetch_trends_enabled", "responsive_web_grok_analyze_post_followups_enabled", "responsive_web_jetfuel_frame", "responsive_web_grok_share_attachment_enabled", "articles_preview_enabled", "responsive_web_edit_tweet_api_enabled", "graphql_is_translatable_rweb_tweet_is_translatable_enabled", "view_counts_everywhere_api_enabled", "longform_notetweets_consumption_enabled", "responsive_web_twitter_article_tweet_consumption_enabled", "tweet_awards_web_tipping_enabled", "responsive_web_grok_show_grok_translated_post", "responsive_web_grok_analysis_button_from_backend", "creator_subscriptions_quote_tweet_preview_enabled", "freedom_of_speech_not_reach_fetch_enabled", "standardized_nudges_misinfo", "tweet_with_visibility_results_prefer_gql_limited_actions_policy_enabled", "longform_notetweets_rich_text_read_enabled", "longform_notetweets_inline_media_enabled", "responsive_web_grok_image_annotation_enabled", "responsive_web_grok_imagine_annotation_enabled", "responsive_web_grok_community_note_auto_translation_is_enabled", "responsive_web_enhance_cards_enabled"],
    //                fieldToggles: ["withAuxiliaryUserLabels", "withArticleRichContentState", "withArticlePlainText", "withGrokAnalyze", "withDisallowedReplyControls"]
    //            }
    //        }
    //    """
    private final static String BASE_URL = "https://x.com/i/api/graphql/%s/TweetDetail";
    private final static String QUERY_ID = "iFEr5AcP121Og4wx9Yqo3w";
    private final static String IMAGE_TYPE = "photo";
    private final static String VIDEO_TYPE = "video";

    @Override
    public Result<ParserResp> execute(String url) throws Exception {
        String id = UrlUtil.getId(url, "status", null);
        if (StrUtil.isBlank(id)) {
            return Result.failure(ErrorCode.PARSER_NOT_GET_ID);
        }
        ForestProxy proxy = proxyConfig.proxy();
        String response = Forest.get(String.format(BASE_URL, QUERY_ID))
                .setUserAgent(CommonUtil.getUserAgent(UserAgentPlatformEnum.DEFAULT))
                .proxy(proxy)
                .addQuery("features", URLUtil.encode(JSONUtil.toJsonStr(new TwitterReqFeatureSwitches())))
                .addQuery("fieldToggles", URLUtil.encode(JSONUtil.toJsonStr(new TwitterReqFieldToggles())))
                .addQuery("variables", URLUtil.encode(JSONUtil.toJsonStr(new TwitterReqVariables(id))))
                .addCookie(new ForestCookie("auth_token", authToken))
                .addCookie(new ForestCookie("ct0", xCsrfToken))
                .addHeader("authorization", Authorization)
                .addHeader("x-csrf-token", xCsrfToken)
                .executeAsString();
        if (StrUtil.isBlank(response)) {
            log.error("解析链接：{} 失败，返回结果：{}", url, response);
            return Result.failure(ErrorCode.PARSER_FAILED);
        }
        JSONObject jsonObject = JSONUtil.parseObj(response);
        JSONArray instructions = jsonObject.getByPath("data['threaded_conversation_with_injections_v2'].instructions", JSONArray.class);
        if (instructions == null || instructions.isEmpty()) {
            log.error("解析链接：{} 失败，返回结果：{}", url, response);
            return Result.failure(ErrorCode.PARSER_PARSE_MEDIA_INFO_FAILED);
        }
        return extract(instructions);
    }

    private Result<ParserResp> extract(JSONArray instructions) {
        ParserResp result = new ParserResp();
        instructions.toList(JSONObject.class).forEach(item -> {
            JSONArray entries = item.get("entries", JSONArray.class);
            if (entries != null && !entries.isEmpty()) {
                JSONObject entity = entries.get(0, JSONObject.class);
                extractInfo(entity, result);
                extractData(entity, result);
            }
        });
        ParserResultUtils.resetCover(result);
        return Result.success(result);
    }

    private void extractInfo(JSONObject item, ParserResp result) {
        // title content.itemContent['tweet_results'].result.legacy['full_text']
        result.setTitle(item.getByPath("content.itemContent['tweet_results'].result.tweet.legacy['full_text']", String.class));
        result.getAuthor()
                .setNickname(item.getByPath("content.itemContent['tweet_results'].result.tweet.core['user_results'].result.core['screen_name']", String.class))
                .setAvatar(item.getByPath("content.itemContent['tweet_results'].result.tweet.core['user_results'].result.avatar['image_url']", String.class));
    }

    private void extractData(JSONObject item, ParserResp result) {
        Optional.ofNullable(item.getByPath("content.itemContent['tweet_results'].result.tweet.legacy.entities.media", JSONArray.class))
                .ifPresent(node -> node.toList(JSONObject.class).forEach(media -> {
                    int width = media.getByPath("original_info.width", Integer.class);
                    int height = media.getByPath("original_info.height", Integer.class);
                    String type = media.getStr("type");
                    if (StrUtil.isBlank(type)) {
                        return;
                    }
                    if (VIDEO_TYPE.equals(type)) {
                        JSONArray variants = media.getByPath("video_info.variants", JSONArray.class);
                        if (variants != null && !variants.isEmpty()) {
                            variants.toList(JSONObject.class).forEach(variant -> {
                                result.getMedias().add(
                                        new ParserResp.Media()
                                                .setType(MediaTypeEnum.VIDEO)
                                                .setUrl(variant.getStr("url"))
                                                .setHeight(height)
                                                .setWidth(width)
                                );
                            });
                        }
                    }
                    if (IMAGE_TYPE.equals(type)) {
                        result.getMedias().add(
                                new ParserResp.Media()
                                        .setType(MediaTypeEnum.IMAGE)
                                        .setUrl(media.getStr("media_url_https"))
                                        .setHeight(height)
                                        .setWidth(width)
                        );
                    }
                }));
    }
}

@Data
class TwitterReqFeatureSwitches {

    @Alias("rweb_video_screen_enabled")
    private final boolean rwebVideoScreenEnabled = false;

    @Alias("payments_enabled")
    private final boolean paymentsEnabled = false;

    @Alias("rweb_xchat_enabled")
    private final boolean rwebXchatEnabled = false;

    @Alias("profile_label_improvements_pcf_label_in_post_enabled")
    private final boolean profileLabelImprovementsPcfLabelInPostEnabled = true;

    @Alias("rweb_tipjar_consumption_enabled")
    private final boolean rwebTipjarConsumptionEnabled = true;

    @Alias("verified_phone_label_enabled")
    private final boolean verifiedPhoneLabelEnabled = false;

    @Alias("creator_subscriptions_tweet_preview_api_enabled")
    private final boolean creatorSubscriptionsTweetPreviewApiEnabled = true;

    @Alias("responsive_web_graphql_timeline_navigation_enabled")
    private final boolean responsiveWebGraphqlTimelineNavigationEnabled = true;

    @Alias("responsive_web_graphql_skip_user_profile_image_extensions_enabled")
    private final boolean responsiveWebGraphqlSkipUserProfileImageExtensionsEnabled = false;

    @Alias("premium_content_api_read_enabled")
    private final boolean premiumContentApiReadEnabled = false;

    @Alias("communities_web_enable_tweet_community_results_fetch")
    private final boolean communitiesWebEnableTweetCommunityResultsFetch = true;

    @Alias("c9s_tweet_anatomy_moderator_badge_enabled")
    private final boolean c9sTweetAnatomyModeratorBadgeEnabled = true;

    @Alias("responsive_web_grok_analyze_button_fetch_trends_enabled")
    private final boolean responsiveWebGrokAnalyzeButtonFetchTrendsEnabled = false;

    @Alias("responsive_web_grok_analyze_post_followups_enabled")
    private final boolean responsiveWebGrokAnalyzePostFollowupsEnabled = true;

    @Alias("responsive_web_jetfuel_frame")
    private final boolean responsiveWebJetfuelFrame = true;

    @Alias("responsive_web_grok_share_attachment_enabled")
    private final boolean responsiveWebGrokShareAttachmentEnabled = true;

    @Alias("articles_preview_enabled")
    private final boolean articlesPreviewEnabled = true;

    @Alias("responsive_web_edit_tweet_api_enabled")
    private final boolean responsiveWebEditTweetApiEnabled = true;

    @Alias("graphql_is_translatable_rweb_tweet_is_translatable_enabled")
    private final boolean graphqlIsTranslatableRwebTweetIsTranslatableEnabled = true;

    @Alias("view_counts_everywhere_api_enabled")
    private final boolean viewCountsEverywhereApiEnabled = true;

    @Alias("longform_notetweets_consumption_enabled")
    private final boolean longformNotetweetsConsumptionEnabled = true;

    @Alias("responsive_web_twitter_article_tweet_consumption_enabled")
    private final boolean responsiveWebTwitterArticleTweetConsumptionEnabled = true;

    @Alias("tweet_awards_web_tipping_enabled")
    private final boolean tweetAwardsWebTippingEnabled = false;

    @Alias("responsive_web_grok_show_grok_translated_post")
    private final boolean responsiveWebGrokShowGrokTranslatedPost = true;

    @Alias("responsive_web_grok_analysis_button_from_backend")
    private final boolean responsiveWebGrokAnalysisButtonFromBackend = true;

    @Alias("creator_subscriptions_quote_tweet_preview_enabled")
    private final boolean creatorSubscriptionsQuoteTweetPreviewEnabled = false;

    @Alias("freedom_of_speech_not_reach_fetch_enabled")
    private final boolean freedomOfSpeechNotReachFetchEnabled = true;

    @Alias("standardized_nudges_misinfo")
    private final boolean standardizedNudgesMisinfo = true;

    @Alias("tweet_with_visibility_results_prefer_gql_limited_actions_policy_enabled")
    private final boolean tweetWithVisibilityResultsPreferGqlLimitedActionsPolicyEnabled = true;

    @Alias("longform_notetweets_rich_text_read_enabled")
    private final boolean longformNotetweetsRichTextReadEnabled = true;

    @Alias("longform_notetweets_inline_media_enabled")
    private final boolean longformNotetweetsInlineMediaEnabled = true;

    @Alias("responsive_web_grok_image_annotation_enabled")
    private final boolean responsiveWebGrokImageAnnotationEnabled = true;

    @Alias("responsive_web_grok_imagine_annotation_enabled")
    private final boolean responsiveWebGrokImagineAnnotationEnabled = true;

    @Alias("responsive_web_grok_community_note_auto_translation_is_enabled")
    private final boolean responsiveWebGrokCommunityNoteAutoTranslationIsEnabled = false;

    @Alias("responsive_web_enhance_cards_enabled")
    private final boolean responsiveWebEnhanceCardsEnabled = false;

}

@Data
class TwitterReqFieldToggles {

    @Alias("withArticleRichContentState")
    private final boolean withArticleRichContentState = true;

    @Alias("withArticlePlainText")
    private final boolean withArticlePlainText = false;

    @Alias("withGrokAnalyze")
    private final boolean withGrokAnalyze = false;

    @Alias("withDisallowedReplyControls")
    private final boolean withDisallowedReplyControls = false;
}

@Data
class TwitterReqVariables {

    @Alias("focalTweetId")
    private String focalTweetId;

    @Alias("with_rux_injections")
    private final boolean withRuxInjections = false;

    @Alias("rankingMode")
    private final String rankingMode = "Relevance";

    @Alias("includePromotedContent")
    private final boolean includePromotedContent = true;

    @Alias("withCommunity")
    private final boolean withCommunity = true;

    @Alias("withQuickPromoteEligibilityTweetFields")
    private final boolean withQuickPromoteEligibilityTweetFields = true;

    @Alias("withBirdwatchNotes")
    private final boolean withBirdwatchNotes = true;

    @Alias("withVoice")
    private final boolean withVoice = true;

    public TwitterReqVariables(String focalTweetId) {
        this.focalTweetId = focalTweetId;
    }

}