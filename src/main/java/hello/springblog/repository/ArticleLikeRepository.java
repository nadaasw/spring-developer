package hello.springblog.repository;

import hello.springblog.domain.Like;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleLikeRepository extends JpaRepository<Like, Long> {
    boolean existsByUserIdAndArticleId(Long userId, Long articleId);
    void deleteByUserIdAndArticleId(Long userId, Long articleId);
    long countByArticleId(Long articleId);
}
