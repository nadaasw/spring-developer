package hello.springblog.controller;

import hello.springblog.domain.Article;
import hello.springblog.dto.ArticleListViewResponse;
import hello.springblog.dto.ArticleViewResponse;
import hello.springblog.service.BlogService;
import hello.springblog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class BlogViewController {
    private final BlogService blogService;
    private final UserService userService;

    @GetMapping("/articles")
    public String getArticles(Model model) {
        List<ArticleListViewResponse> articles = blogService.findAll().stream()
                .map(ArticleListViewResponse::new)
                .toList();

        model.addAttribute("articles", articles);

        return "articleList";
    }

    @GetMapping("/articles/{id}")
    public String getArticle(@PathVariable("id") Long id, Authentication authentication, Model model) {
        Long userId = null;

        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            userId = userService.findByEmail(email).getId();
        }

        Article article = blogService.findById(id);

        boolean likedByUser = (userId != null)
                && blogService.hasLikeByUser(userId, id);

        ArticleViewResponse response =
                new ArticleViewResponse(article, likedByUser);

        model.addAttribute("article", response);

        return "article";
    }

    @GetMapping("/new-article")
    // id 키를 가진 쿼리 파라미터의 값을 id 변수에 매핑(id는 없을 수도 있음)
    public String newArticle(@RequestParam(required = false) Long id, Model model) {
        if(id == null) {
            model.addAttribute("article", new ArticleViewResponse());
        }else{
            Article article = blogService.findById(id);
            model.addAttribute("article", new ArticleViewResponse(article, false));
        }

        return "newArticle";
    }
}
