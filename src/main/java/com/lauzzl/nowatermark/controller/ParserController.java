package com.lauzzl.nowatermark.controller;

import com.lauzzl.nowatermark.base.domain.Result;
import com.lauzzl.nowatermark.base.model.req.ParserReq;
import com.lauzzl.nowatermark.base.model.resp.ParserResp;
import com.lauzzl.nowatermark.service.ParserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("parser")
@Tag(name = "解析器")
public class ParserController {

    @Resource
    private ParserService parserService;

    @Operation(summary = "解析")
    @PostMapping("executor")
    @ResponseBody
    public Result<ParserResp> executor(@RequestBody @Validated ParserReq req) throws Exception {
        return parserService.execute(req);
    }


}
