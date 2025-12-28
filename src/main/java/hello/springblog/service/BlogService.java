package hello.springblog.service;

import hello.springblog.domain.Article;
import hello.springblog.domain.Comment;
import hello.springblog.domain.Like;
import hello.springblog.domain.User;
import hello.springblog.dto.AddArticleRequest;
import hello.springblog.dto.AddCommentRequest;
import hello.springblog.dto.UpdateArticleRequest;
import hello.springblog.dto.UpdateCommentRequest;
import hello.springblog.repository.ArticleLikeRepository;
import hello.springblog.repository.BlogRepository;
import hello.springblog.repository.CommentRepository;
import hello.springblog.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BlogService {

    private final BlogRepository blogRepository;
    private final CommentRepository commentRepository;
    private final ArticleLikeRepository articleLikeRepository;
    private final UserRepository userRepository;

    public Article save(AddArticleRequest request, String userName) {
        return blogRepository.save(request.toEntity(userName));
    }

    public List<Article> findAll() {
        return blogRepository.findAll();
    }

    public Article findById(Long id) {
        return blogRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("not found:" + id));
    }

    public void delete(Long id) {
        Article article = blogRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("not found:" + id));

        authorizeArticleAuthor(article);
        blogRepository.delete(article);
    }

    @Transactional
    public Article update(long id, UpdateArticleRequest request) {
        Article article = blogRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("not found:" + id));

        authorizeArticleAuthor(article);
        article.update(request.getTitle(), request.getContent());

        return article;
    }

    // 게시글을 작성한 유저인지 확인
    private static void authorizeArticleAuthor(Article article) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        if(!article.getAuthor().equals(userName)) {
            throw new IllegalArgumentException("not authorized");
        }
    }

    // 댓글 추가
    public Comment addComment(AddCommentRequest request, String userName) {
        Article article = blogRepository.findById(request.getArticleId())
                .orElseThrow(()-> new IllegalArgumentException("not found:" + request.getArticleId()));

        return commentRepository.save(request.toEntity(userName, article));
    }

    // 댓글 삭제
    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("not found:" + id));

        authorizeCommentAuthor(comment);
        commentRepository.delete(comment);
    }

    // 댓글 수정
    @Transactional
    public Comment updateComment(Long id, UpdateCommentRequest request) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("not found:" + id));

        authorizeCommentAuthor(comment);
        comment.update(request.getContent());

        return comment;
    }



    // 댓글을 작성한 유저인지 확인
    private static void authorizeCommentAuthor(Comment comment) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        if(!comment.getAuthor().equals(userName)) {
            throw new IllegalArgumentException("not authorized");
        }
    }

    // 좋아요 기능
    @Transactional
    public void like(Long userId, Long articleId) {
        if(articleLikeRepository.existsByUserIdAndArticleId(userId, articleId)) {
            throw new IllegalArgumentException("already liked");
        }

        User user = userRepository.findById(userId).get();
        Article article = blogRepository.findById(articleId).get();

        articleLikeRepository.save(Like.builder()
                .user(user)
                .article(article)
                .build());

    }

    // 좋아요 삭제
    @Transactional
    public void cancelLike(Long userId, Long articleId) {
        if(!articleLikeRepository.existsByUserIdAndArticleId(userId, articleId)) {
            throw new IllegalArgumentException("not liked");
        }

        articleLikeRepository.deleteByUserIdAndArticleId(userId, articleId);
    }

    public boolean hasLikeByUser(Long userId, Long articleId) {
        return articleLikeRepository.existsByUserIdAndArticleId(userId, articleId);
    }
}
