package com.yg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yg.entity.Datasource;
import com.yg.mapper.DatasourceMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DatasourceService extends ServiceImpl<DatasourceMapper, Datasource> {

    /**
     *  按数据源名称查询
     *
     * @param name
     * @return
     */
    public Datasource queryDataSourceByName(String name) {
        LambdaQueryWrapper<Datasource> wrapper = new QueryWrapper<Datasource>().lambda();
        wrapper.eq(Datasource::getName, name);
        List<Datasource> list = this.list(wrapper);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }

        return list.get(0);
    }
}
