/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.putoi.backend.repository;

import com.putoi.backend.models.NewsImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author alfia
 */
@Repository
public interface NewsImageRepository extends JpaRepository<NewsImage, Long>{

    List<NewsImage> findByNews_Id(Long newsId);

    void deleteByNews_Id(Long newsId);

    boolean existsByNews_Id(Long newsId);
}
