/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.putoi.backend.repository;

import com.putoi.backend.models.EmailOTP;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author alfia
 */
public interface EmailOTPRepositorry extends JpaRepository<EmailOTP, Long>{
     Optional<EmailOTP> findByEmail(String email);
     boolean existsByEmail(String email);
}
