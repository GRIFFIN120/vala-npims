package com.vala.carbon.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import com.vala.commons.bean.data.VData;
import com.vala.service.RService;
import org.rosuda.REngine.Rserve.RConnection;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RegressionService {


    public double[]  getCoefficients(VData Xi, Map<String, Double> coreMap, int breakPoint, int ncomp) throws Exception {

        List<String> names = new ArrayList<>();
        RService rService = new RService();
        RConnection con = rService.getConnection();
        for (int i = 0; i < Xi.titles.size(); i++) {
            Object title = Xi.titles.get(i);
            Double[] columnBy = Xi.getColumnBy(title);
            double[] column = VData.converter(columnBy);
            double[] subX = ArrayUtil.sub(column, breakPoint, Xi.ROW_COUNT);
            String name = "x"+i;
            names.add(name);
            con.assign(name,subX);
        }
        List<String> categories = Xi.getCategories();
        double[] y = new double[categories.size()];
        for (int i = 0; i < categories.size(); i++) {
            String s = categories.get(i);
            Double yi = coreMap.get(s);
            y[i] = yi;
        }
        double[] subY = ArrayUtil.sub(y, breakPoint, Xi.ROW_COUNT);

        String fun = "y~" + CollectionUtil.join(names,"+");
        con.assign("y",subY);
        con.voidEval("library(pls)");
        con.voidEval("fit<-plsr("+fun+",validation='LOO', ncomp="+ncomp+",jackknife=TRUE)");
        con.voidEval("z = as.vector(coef(fit))");
        double[] ds = con.eval("z").asDoubles();
        rService.close();

        return ds;

    }



}
