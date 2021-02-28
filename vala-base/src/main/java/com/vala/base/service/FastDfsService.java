package com.vala.base.service;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.fdfs.ThumbImageConfig;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadCallback;
import com.github.tobato.fastdfs.exception.FdfsUnsupportStorePathException;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.vala.commons.util.BeanUtils;
import com.vala.base.entity.FileColumn;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Getter
@Service
public class FastDfsService {


    private Map<String,String> contentTypes = new HashMap<String,String>();

    @PostConstruct
    public void initContentTypes(){
        contentTypes.put("image/jpeg",".jpg");
        contentTypes.put("image/png",".png");
    }


    @Value("${fdfs.web-server-url}")
    String location;


    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    private ThumbImageConfig thumbImageConfig;






    public byte[] read(String path ){
        if(path==null) return new byte[0];
        StorePath storePath = this.parseFromUrl(path);
        byte[] bytes = storageClient.downloadFile(storePath.getGroup(), storePath.getPath(), new DownloadCallback<byte[]>() {
            @Override
            public byte[] recv(InputStream ins) throws IOException {
                return IOUtils.toByteArray(ins);
            }
        });
        return bytes;
    }

    public String upload(MultipartFile file) throws Exception {
        String[] strs = this.getNameAndExtension(file.getOriginalFilename());
        String url =  this.upload(file.getBytes(),strs[1]);
        System.out.println(location+url);
        return url;
    }



    public String upload(byte[] bytes, String fix){
        StorePath storePath = storageClient.uploadFile(new ByteArrayInputStream(bytes), bytes.length, fix, null);
        String url = storePath.getFullPath();
        System.out.println(location+url);
        return url;
    }

    public boolean delete(String path){
        if(StringUtils.isEmpty(path)){
            return false;
        }
        try {
            StorePath storePath = this.parseFromUrl(path);
            storageClient.deleteFile(storePath.getGroup(), storePath.getPath());
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    public void deleteBeanFile(Object bean) throws Exception{
        if(bean==null) return;
        List<Field> completeFields = BeanUtils.getCompleteFields(bean.getClass());
        for (Field f : completeFields) {
            if(f.isAnnotationPresent(FileColumn.class)){
                String path = (String) f.get(bean);
                if(StringUtils.isNotEmpty(path)){
                    boolean flag = this.delete(path);
//                    throw new Exception("无法删除文件:"+path);
                    if(!flag) System.out.println("无法删除文件:"+path);
                }
            }
        }
    }



    public  String modify(String path, byte[] bytes, String fix){
        delete(path);
        return upload(bytes,fix);
    }





    private StorePath parseFromUrl(String path) {
        try {
            String filePath = path==null?null:path.substring(path.indexOf("group"),path.length());
            Validate.notNull(filePath, "解析文件路径不能为空", new Object[0]);
            String group = getGroupName(filePath);
            int pathStartPos = filePath.indexOf(group) + group.length() + 1;
            String rpath = filePath.substring(pathStartPos, filePath.length());
            return new StorePath(group, rpath);
        } catch (Exception e) {
            return null;
        }
    }

    private String getGroupName(String filePath) {
        String[] paths = filePath.split("/");
        if (paths.length == 1) {
            throw new FdfsUnsupportStorePathException("解析文件路径错误,有效的路径样式为(group/path) 而当前解析路径为".concat(filePath));
        } else {
            String[] var2 = paths;
            int var3 = paths.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                String item = var2[var4];
                if (item.indexOf("group") != -1) {
                    return item;
                }
            }
            throw new FdfsUnsupportStorePathException("解析文件路径错误,被解析路径url没有group,当前解析路径为".concat(filePath));
        }
    }

    public static String getFileType(String fileName){
        String type = null;
        if(fileName.indexOf(".")!=-1){
            type = fileName.substring(fileName.lastIndexOf(".")+1,fileName.length());
        }
        return type;
    }



    public String[] uploadImage(MultipartFile mfile) throws IOException {

        String fileName = mfile.getOriginalFilename();
        String type = getFileType(fileName);
        type = type==null?"png":type;
        StorePath storePath = this.storageClient.uploadImageAndCrtThumbImage(
                mfile.getInputStream(), mfile.getSize(), type, null);


        String path = storePath.getFullPath();
        String thumb = storePath.getGroup()+"/"+
                thumbImageConfig.getThumbImagePath(storePath.getPath());
        return new String[]{path,thumb};
    }


    public String[] uploadImage(byte[] bytes, String fix) throws IOException {

        StorePath storePath = this.storageClient.uploadImageAndCrtThumbImage(
                new ByteArrayInputStream(bytes), bytes.length, fix, null);
        String path = storePath.getFullPath();
        String thumb = storePath.getGroup()+"/"+
                thumbImageConfig.getThumbImagePath(storePath.getPath());
        return new String[]{path,thumb};
    }



    public static final String DOT = ".";
    public String[] getNameAndExtension(String fileName) {
        if (StringUtils.INDEX_NOT_FOUND == StringUtils.indexOf(fileName, DOT))
            return new String[]{fileName, StringUtils.EMPTY};
        int DOT_INDEX = StringUtils.lastIndexOf(fileName, DOT);
        String name = StringUtils.substring(fileName,0,DOT_INDEX).trim();
        String ext = StringUtils.substring(fileName,DOT_INDEX+1).trim();
        return new String[]{name,ext};
    }

}
