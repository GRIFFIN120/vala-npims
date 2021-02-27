package com.vala.framework.file.controller;

import com.vala.base.controller.BaseController;
import com.vala.base.entity.FileColumn;
import com.vala.commons.bean.ResponseResult;
import com.vala.commons.util.BeanUtils;
import com.vala.framework.file.entity.EditorImage;
import com.vala.framework.file.entity.FileEntity;
import com.vala.framework.file.entity.ImageEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileBaseController  <T extends FileEntity> extends BaseController<T>{




    @Transactional
    @RequestMapping(value = "/editor/image", produces = "application/json;charset=UTF-8")
    public Map<String,Object> image(@RequestPart(value="upload") MultipartFile file, @RequestParam Integer id,@RequestParam String entity) throws Exception {

        EditorImage image = new EditorImage();

        String[] arr = this.fastDfsService.uploadImage(file);
        String url = arr[0];
        String thumb = arr[1];
        // 4. 获取其他参数
        String fileName = file.getOriginalFilename();
        String[] strs = this.fastDfsService.getNameAndExtension(fileName);;
        String name = strs[0];
        String extension = strs[1];
        Long size = file.getSize();
        // 5. 更新bean
        image.setBeanEntity(entity);
        image.setBeanId(id);
        image.setServer(this.fastDfsService.getLocation());
        image.setUrl(url);
        image.setThumb(thumb);
        image.setFileName(fileName);
        image.setSize(size);
        image.setExtension(extension);
        image.setName(name);

        this.baseService.getRepo().save(image);

        Map<String,Object> map = new HashMap<>();
        map.put("uploaded",true);
        map.put("url",image.getServer()+image.getUrl());
        map.put("fileName",image.getFileName());
        return map;
    }



    @ResponseBody
    @RequestMapping("/set/text")
    public ResponseResult setText( @RequestBody T bean) throws Exception {
        T loaded =  this.baseService.get(bean.id);
        String OLD_URL = null;
//
        List<Field> list = BeanUtils.getFieldsByAnnotation(this.domain, FileColumn.class);
        for (Field field : list) {
            String extension = field.getAnnotation(FileColumn.class).type();
            extension = StringUtils.isEmpty(extension)? "txt" : extension;
            String text = (String) field.get(bean);
            if(text!=null){


                if(loaded!=null) OLD_URL = (String) field.get(loaded);

                String url = this.fastDfsService.upload(text.getBytes(),extension);
                field.set(bean,url);
                bean.setServer(this.fastDfsService.getLocation());
                if(field.getName().equalsIgnoreCase("url")){
                    String fileName = String.valueOf(new Date().getTime())+"."+extension;
                    bean.setExtension(extension);
                    bean.setSize(Long.valueOf(text.length()));
                    bean.setFileName(fileName);
                }
            }
        }

        this.baseService.saveOrUpdate(bean);
        if(StringUtils.isNotEmpty(OLD_URL)) this.fastDfsService.delete(OLD_URL);

        return new ResponseResult("上传成功");
    }


    @ResponseBody
    @RequestMapping("/blob/{id}")
    public ResponseResult base64(@PathVariable Integer id, @RequestPart(value="file") MultipartFile file) throws Exception {

//        if(this.domain.isAssignableFrom(ImageEntity.class)){
            T bean = this.baseService.get(id);;

            ImageEntity t = (ImageEntity) bean;

            String oldThumb = t.getThumb();
            String oldUrl = t.getUrl();


            String location = this.fastDfsService.getLocation();
            String[] args = this.fastDfsService.uploadImage(file);

            String url = (args[0]);
            String thumb = (args[1]);
            Long size = file.getSize();
            String extension = "png";
            String fileName = file.getOriginalFilename();

            t.setFileName(fileName);
            t.setExtension(extension);
            t.setSize(size);
            t.setServer(location);
            t.setUrl(url);
            t.setThumb(thumb);

            this.baseService.saveOrUpdate(bean);

            this.fastDfsService.delete(oldThumb);
            this.fastDfsService.delete(oldUrl);

            return new ResponseResult("上传成功");
//        }else {
//            return new ResponseResult(400,"实体类型错误");
//        }
    }


    @Transactional
    @ResponseBody
    @RequestMapping("/set/image/{id}")
    public ResponseResult setImage(@PathVariable Integer id, @RequestPart(value = "file") MultipartFile file) throws Exception {


        if(file==null) return new ResponseResult(600,"文件不存在");
        if(ImageEntity.class.isAssignableFrom(this.domain)){
            T t = id==-1? this.baseService.newInstance(): this.baseService.get(id);
            // 1. 获取bean
            ImageEntity bean = (ImageEntity) t;
            // 2. 删除原有文件
            String url = bean.getUrl();
            String thumb = bean.getThumb();
            if(StringUtils.isEmpty(url)) this.fastDfsService.delete(url);
            if(StringUtils.isEmpty(thumb)) this.fastDfsService.delete(thumb);
            // 3. 上传文件
            String[] arr = this.fastDfsService.uploadImage(file);
            url = arr[0];
            thumb = arr[1];
            // 4. 获取其他参数
            String fileName = file.getOriginalFilename();
            String[] strs = this.fastDfsService.getNameAndExtension(fileName);;
            String extension = strs[1];
            String name = strs[0];
            Long size = file.getSize();
            // 5. 更新bean
            bean.setFileName(fileName);
            bean.setSize(size);
            bean.setExtension(extension);
            bean.setUrl(url);
            bean.setThumb(thumb);
            if(StringUtils.isEmpty(t.getName())){
                t.setName(name);
            }
            bean.setServer(this.fastDfsService.getLocation());
            this.baseService.saveOrUpdate(t);
            // 6.返回
            return new ResponseResult(200,"图片上传成功", bean);
        }else {
            return new ResponseResult(400,"非图片实体类型");
        }
    }

    @Transactional
    @ResponseBody
    @RequestMapping("/set/file/{id}")
    public ResponseResult setFile(@PathVariable Integer id, @RequestPart(value = "file") MultipartFile file) throws Exception {
        if(file==null) return new ResponseResult(600,"文件不存在");
        // 1. 获取bean
        T bean = id==-1? this.baseService.newInstance(): this.baseService.get(id);
        // 2. 删除原有文件
        String url = bean.getUrl();
        if(StringUtils.isEmpty(url)) this.fastDfsService.delete(url);
        // 3. 上传文件
        url = this.fastDfsService.upload(file);
        // 4. 获取其他参数
        String fileName = file.getOriginalFilename();
        String[] strs = this.fastDfsService.getNameAndExtension(fileName);;
        String extension = strs[1];
        String name = strs[0];
        Long size = file.getSize();
        // 5. 更新bean
        bean.setFileName(fileName);
        bean.setSize(size);
        bean.setExtension(extension);
        bean.setUrl(url);
        if(StringUtils.isEmpty(bean.getName())){
            bean.setName(name);
        }
        bean.setServer(this.fastDfsService.getLocation());
        this.baseService.saveOrUpdate(bean);
        // 6.返回
        return new ResponseResult("上传成功");
    }









}
