package com.srihari.url_shortener.Repositories;


import com.srihari.url_shortener.Entities.ShortURL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShortUrlRepo extends JpaRepository<ShortURL, Long> {
    @Query("select su from ShortURL su where su.isPrivate = false order by su.createdAt desc")
    List<ShortURL> findPublicShortUrls();
    boolean existsByShortKey(String shortKey);
    Optional<ShortURL> findByShortKey(String shortKey);

    @Query("select su from ShortURL su where su.isPrivate = false and su.createdBy.id = :userId order by su.createdAt desc")
    List<ShortURL> findPublicShortUrlsByUser(Long userId);
}
