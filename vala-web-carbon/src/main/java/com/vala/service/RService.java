package com.vala.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import java.util.ArrayList;
import java.util.List;

public class RService {
    private RConnection connection;

    public String url = "8.131.72.230"; //"120.27.8.186"    "8.131.72.230"

    public RService() throws Exception {
        RConnection re = new RConnection(url);
        REXP x = re.eval("R.version.string");
        System.out.println(x.asString());
        this.connection = re;
    }

    public void close(){
        this.connection.close();
    }


    public Double forecast(Double[] column, int[] c, int size) throws Exception {
        try {
            double[] x = new double[column.length];
            for (int i = 0; i < column.length; i++) {
                x[i] = column[i];
            }
            int[] h = new int[]{size};
            connection.voidEval("library(forecast)");
            connection.assign("x",x);
            connection.assign("c",c);
            connection.assign("h",h);
            connection.voidEval("fore = forecast(Arima(x,c),h=h)$mean;");
            connection.voidEval("ret = as.vector(fore)");
            double d = connection.eval("ret[length(ret)]").asDouble();
            return d;
        }catch (Exception e){
            connection.close();
            connection = new RConnection(url);
            return column[column.length-1]; // ?check
        }
    }


    public Double[] forest(double[][] data) throws Exception {
        connection.voidEval("library(randomForest)");
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
    private Double[] forest() throws Exception {
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
        Double[] ret = new Double[res.length];
        for (int i = 0; i < res.length; i++) {
            ret[i] = res[i];
        }
        return ret;
    }
}
