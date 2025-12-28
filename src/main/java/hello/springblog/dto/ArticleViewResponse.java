package hello.springblog.dto;

import hello.springblog.domain.Article;
import hello.springblog.domain.Comment;
import hello.springblog.domain.Like;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Getter
public class ArticleViewResponse {

    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String author;
    private List<Comment> comments;
    private int likeCount;
    private boolean likeByUser;

    public ArticleViewResponse(Article article, boolean likeByUser) {
        this.id = article.getId();
        this.title = article.getTitle();
        this.content = article.getContent();
        this.createdAt = article.getCreatedAt();
        this.updatedAt = article.getUpdatedAt();
        this.author = article.getAuthor();
        this.comments = article.getComments();
        this.likeCount = article.getLikes().size();
        this.likeByUser = likeByUser;
    }

}
