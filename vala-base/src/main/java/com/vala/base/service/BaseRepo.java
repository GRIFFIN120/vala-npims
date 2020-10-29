package com.vala.base.service;

import com.vala.base.entity.BaseEntity;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

public interface BaseRepo extends JpaRepositoryImplementation<BaseEntity, Integer> {
}
