package com.lauzzl.nowatermark.base.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "解析请求参数")
public class ParserReq {

    @Schema(description = "视频链接")
    @NotBlank(message = "视频链接不能为空")
    private String url;

}
