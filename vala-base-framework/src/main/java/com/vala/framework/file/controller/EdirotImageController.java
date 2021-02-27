package com.vala.framework.file.controller;

import com.vala.framework.file.entity.EditorImage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/editor-image")
public class EdirotImageController extends FileBaseController<EditorImage> {
}
