package hello.springblog.controller;

import hello.springblog.config.jwt.TokenProvider;
import hello.springblog.domain.Article;
import hello.springblog.domain.Comment;
import hello.springblog.domain.User;
import hello.springblog.dto.*;
import hello.springblog.service.BlogService;
import hello.springblog.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class BlogController {

    private final BlogService blogService;
    private final UserService userService;

    @PostMapping("/api/articles")
    public ResponseEntity<Article> addArticle(@RequestBody AddArticleRequest request, Principal principal) {
        Article savedArticle = blogService.save(request, principal.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(savedArticle);
    }

    @GetMapping("/api/articles")
    public ResponseEntity<List<ArticleResponse>> findAllArticles() {
        List<ArticleResponse> articles = blogService.findAll()
                .stream()
                .map(ArticleResponse::new)
                .toList();
        return ResponseEntity.ok().body(articles);
    }

    @GetMapping("/api/articles/{id}")
    public ResponseEntity<ArticleResponse> findArticle(@PathVariable Long id) {
        Article article = blogService.findById(id);

        return ResponseEntity.ok().body(new ArticleResponse(article));
    }

    @DeleteMapping("/api/articles/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        blogService.delete(id);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/api/articles/{id}")
    public ResponseEntity<Article> updateArticle(@PathVariable Long id, @RequestBody UpdateArticleRequest request) {
        Article updatedArticle = blogService.update(id, request);

        return ResponseEntity.ok().body(updatedArticle);
    }

    @PostMapping("/api/comments")
    public ResponseEntity<AddCommentResponse> addComment(@RequestBody AddCommentRequest request, Principal principal) {
        Comment savedComment = blogService.addComment(request, principal.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(new AddCommentResponse(savedComment));
    }

    @DeleteMapping("/api/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        blogService.deleteComment(id);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/api/comments/{id}")
    public ResponseEntity<Comment> updateComment(@PathVariable Long id, @RequestBody UpdateCommentRequest request) {
        Comment updatedComment = blogService.updateComment(id, request);

        return ResponseEntity.ok().body(updatedComment);
    }

    @PostMapping("/api/articles/{id}/like")
    public ResponseEntity<Void> likeArticle(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        Long userId = userService.findByEmail(email).getId();

        blogService.like(userId, id);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/articles/{id}/like")
    public ResponseEntity<Void> unlikeArticle(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        Long userId = userService.findByEmail(email).getId();

        blogService.cancelLike(userId, id);

        return ResponseEntity.ok().build();
    }
}
