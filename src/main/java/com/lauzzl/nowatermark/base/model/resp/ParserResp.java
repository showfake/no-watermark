package com.lauzzl.nowatermark.base.model.resp;

import cn.hutool.core.util.StrUtil;
import com.lauzzl.nowatermark.base.enums.MediaTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "解析响应参数")
@Accessors(chain = true)
public class ParserResp {

    @Schema(description = "视频标题")
    private String title;

    @Schema(description = "作者信息")
    private Author author = new Author();

    @Schema(description = "封面")
    private String cover;

    @Schema(description = "媒体资源")
    private List<Media> medias = new ArrayList<>();


    @Data
    @Schema(description = "作者信息")
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Author {

        @Schema(description = "昵称")
        private String nickname;

        @Schema(description = "头像")
        private String avatar;

    }


    @Data
    @Schema(description = "媒体资源")
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Media {

        @Schema(description = "类型(VIDEO->视频,IMAGE->图集,LIVE->实况,AUDIO->音频)")
        private MediaTypeEnum type;

        @Schema(description = "地址")
        private String url;

        @Schema(description = "高度")
        private Integer height;

        @Schema(description = "宽度")
        private Integer width;

    }

}
