package com.vala.service;

import cn.hutool.core.collection.CollectionUtil;
import org.rosuda.REngine.Rserve.RConnection;

import java.util.ArrayList;
import java.util.List;

public class RService {
    private RConnection connection;

    public double[] forest(double[][] data) throws Exception {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            double[] factor = data[i];
            String name = i==0? "Y" : ("X"+i);
            connection.assign(name,factor);
            list.add(name);
        }
        String line1 = "data = data.frame("+ CollectionUtil.join(list,",")+")";
        connection.voidEval(line1);
        return forest();
    }

    public String url = "8.131.72.230"; //"120.27.8.186"    "8.131.72.230"

    public RService() throws Exception {
        RConnection re = new RConnection(url);
        re.voidEval("library(randomForest)");
        this.connection = re;
    }
    public void close(){
        this.connection.close();
    }


    private double[] forest() throws Exception {
        connection.voidEval("sample = data");
        connection.voidEval("Y=sample$Y");
        connection.voidEval("Q3 = quantile(Y,0.33);Q6 = quantile(Y,0.66);");
        connection.voidEval("low = which(Y<Q3);mid = which(Y>=Q3&Y<Q6);");
        connection.voidEval("temp = rep('high',length(Y));temp[low] = 'low';temp[mid] = 'mid';");
        connection.voidEval("sample[,1] = factor(temp)");
        connection.voidEval("rate = 1;set.seed(123);");
        connection.voidEval("for(i in 1:20){  set.seed(i*234); model = randomForest(sample$Y~.,data=sample,mtry=i,ntree=5000,importance=TRUE);  rate[i] = mean(model$err.rate);}");
        connection.voidEval("mtry = which(rate==min(rate,na.rm=TRUE))");
        connection.voidEval("model = randomForest(sample$Y~.,data=sample,mtry=mtry,ntree=5000,importance=TRUE)");
        connection.voidEval("res = as.vector(model$importance[,5])");
        double[] res = connection.eval("res").asDoubles();
        return res;
    }
}
