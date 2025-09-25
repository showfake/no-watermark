package com.lauzzl.nowatermark.base.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Schema(description = "支持列表响应参数")
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class SupportResp {

    @Schema(description = "平台名称")
    private String name;

    @Schema(description = "平台logo")
    private String logo;

}
