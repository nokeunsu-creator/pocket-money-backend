package com.pocketmoney.repository;

import com.pocketmoney.entity.ProfilePhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProfilePhotoRepository extends JpaRepository<ProfilePhoto, String> {
    List<ProfilePhoto> findAll();
}
