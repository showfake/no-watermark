package com.lauzzl.nowatermark.controller;

import com.lauzzl.nowatermark.base.domain.Result;
import com.lauzzl.nowatermark.factory.enums.Platform;
import com.lauzzl.nowatermark.base.model.resp.SupportResp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("support")
@Tag(name = "支持平台")
public class SupportController {

    @Operation(summary = "支持平台")
    @GetMapping("all")
    @ResponseBody
    public Result<List<SupportResp>> support() {

        return Result.success(
                Arrays.stream(Platform.values())
                        .map(platform -> new SupportResp(platform.getPlatformName(), platform.getLogo()))
                        .toList()
        );
    }

}
