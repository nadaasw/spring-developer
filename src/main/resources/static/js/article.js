//삭제 기능
const deleteButton = document.getElementById('delete-btn');

if (deleteButton) {
    deleteButton.addEventListener('click', event => {
        let id = document.getElementById('article-id').value;
        function success(){
            alert("삭제가 완려되었습니다.")
            location.replace("/articles");
        }
        function fail(){
            alert("삭제 실패했습니다.");
            location.replace("/articles");
        }
        httpRequest("DELETE", "/api/articles/" + id, null, success, fail);
    });
}

const modifyButton = document.getElementById('modify-btn');

if(modifyButton){
    modifyButton.addEventListener('click', event => {
        let params = new URLSearchParams(location.search);
        let id = params.get('id');

        body = JSON.stringify({
            title: document.getElementById("title").value,
            content: document.getElementById("content").value,
        });

        function success(){
            alert("수정 완료되었습니다.");
            location.replace("/articles");
        }
        function fail(){
            alert("수정 실패했습니다.");
            location.replace("/articles/" + id);
        }

        httpRequest("PUT", "/api/articles/" + id, body, success, fail);
    });
}

const createButton = document.getElementById("create-btn")

if(createButton) {
    createButton.addEventListener("click", (event) => {
        body = JSON.stringify({
            title : document.getElementById("title").value,
            content: document.getElementById("content").value,
        });
        function success(){
            alert("등록 완료되었습니다.");
            location.replace("/articles");
        }
        function fail() {
            alert("등록 실패했습니다.");
            location.replace("/articles");
        }
        httpRequest("POST","/api/articles",body, success, fail);
    });
}

// 쿠키를 가져오는 함수
function getCookie(key){
    var result = null;
    var cookie = document.cookie.split(";");
    cookie.some(function (item){
        item = item.replace(" ","");

        var dic = item.split("=");

        if(key === dic[0]){
            result = dic[1];
            return true;
        }
    });
    return result;
}

// HTTP 요청을 보내는 함수
function httpRequest(method, url, body, success, fail){
    fetch(url, {
        method: method,
        headers: {
            // 로컬 스토리지에 엑세스 토큰 값을 가져와 헤더 추가
            Authorization : "Bearer " + localStorage.getItem("access_token"),
            "Content-Type": "application/json",
        },
        body: body,
    }).then((response) => {
        if(response.status === 200 || response.status === 201){
            return success();
        }
        const refresh_token = getCookie("refresh_token");
        if(response.status === 401 && refresh_token){
            fetch("/api/token", {
                method: "POST",
                headers: {
                    Authorization : "Bearer " + localStorage.getItem("access_token"),
                    "Content-Type":"application/json",
                },
                body: JSON.stringify({
                    refreshToken: getCookie("refresh_token"),
                }),
            })
                .then((res) => {
                    if(res.ok){
                        return res.json();
                    }
                })
                .then((result) => {
                    // 재발급이 성공하면 로컬 스토리지값을 새로운 액세스 토큰으로 교체
                    localStorage.setItem("access_token", result.accessToken);
                    httpRequest(method,url, body, success, fail);
                })
                .catch((error) => fail());
        }else{
            return fail();
        }
    });
}

const commentCreateButton = document.getElementById('comment-create-btn');

if(commentCreateButton){
    commentCreateButton.addEventListener('click', event => {
        articleId = document.getElementById('article-id').value;

        body = JSON.stringify({
            articleId: articleId,
            content: document.getElementById('content').value
        });

        function success(){
            alert('등록 완료되었습니다.');
            location.replace('/articles/' + articleId);
        };
        function fail(){
            alert('등록 실패했습니다.');
            location.replace('/articles/' + articleId);
        };

        httpRequest('POST', '/api/comments', body, success, fail)
    });
}

document.addEventListener("click", (e) => {
    const articleId = document.getElementById("article-id")?.value;

    // =========================
    // ✅ 댓글 삭제 (이벤트 위임)
    // =========================
    const delBtn = e.target.closest(".comment-delete-btn");
    if (delBtn) {
        const card = delBtn.closest(".comment-card");
        if (!card) return;

        const commentId = card.querySelector(".comment-id")?.value;
        if (!commentId || !articleId) return;

        httpRequest(
            "DELETE",
            "/api/comments/" + commentId,
            null,
            () => {
                alert("삭제가 완료되었습니다.");
                location.replace("/articles/" + articleId);
            },
            () => {
                alert("삭제 실패했습니다.");
                location.replace("/articles/" + articleId);
            }
        );
        return;
    }

    // ==========================================
    // ✅ 댓글 수정 버튼 -> textarea로 전환 (편집모드)
    // ==========================================
    const modifyBtn = e.target.closest(".comment-modify-btn");
    if (modifyBtn) {
        const card = modifyBtn.closest(".comment-card");
        if (!card) return;

        // 이미 편집 중이면 중복 생성 방지
        if (card.dataset.editing === "true") return;
        card.dataset.editing = "true";

        const contentEl = card.querySelector(".comment-content");
        const originalText = contentEl?.textContent ?? "";

        // 기존 텍스트 숨김
        if (contentEl) contentEl.style.display = "none";

        // textarea 생성
        const textarea = document.createElement("textarea");
        textarea.className = "form-control comment-edit-textarea";
        textarea.rows = 3;
        textarea.value = originalText;

        // 저장/취소 버튼 생성
        const saveBtn = document.createElement("button");
        saveBtn.type = "button";
        saveBtn.className = "btn btn-primary btn-sm comment-save-btn";
        saveBtn.textContent = "저장";

        const cancelBtn = document.createElement("button");
        cancelBtn.type = "button";
        cancelBtn.className = "btn btn-secondary btn-sm comment-cancel-btn ml-1";
        cancelBtn.textContent = "취소";

        // 버튼들 일단 숨김(수정/삭제)
        modifyBtn.style.display = "none";
        const deleteBtnInCard = card.querySelector(".comment-delete-btn");
        if (deleteBtnInCard) deleteBtnInCard.style.display = "none";

        // DOM에 삽입 (textarea -> 저장/취소)
        contentEl?.before(textarea);
        textarea.after(saveBtn);
        saveBtn.after(cancelBtn);

        textarea.focus();
        return;
    }

    // =========================
    // ✅ 댓글 저장 (PUT 호출)
    // =========================
    const saveBtn = e.target.closest(".comment-save-btn");
    if (saveBtn) {
        const card = saveBtn.closest(".comment-card");
        if (!card) return;

        const commentId = card.querySelector(".comment-id")?.value;
        const textarea = card.querySelector(".comment-edit-textarea");
        const newContent = textarea?.value?.trim();

        if (!commentId || !articleId) return;

        if (!newContent) {
            alert("내용을 입력해주세요.");
            textarea?.focus();
            return;
        }

        // ✅ 서버 DTO에 맞게 body 구성
        // 보통은 { "content": "..." } 만 있어도 됨.
        const body = JSON.stringify({
            content: newContent,
            // 필요하면 켜기: articleId: articleId
        });

        httpRequest(
            "PUT",
            "/api/comments/" + commentId,
            body,
            () => {
                alert("수정 완료되었습니다.");
                location.replace("/articles/" + articleId);
            },
            () => {
                alert("수정 실패했습니다.");
                location.replace("/articles/" + articleId);
            }
        );
        return;
    }

    // =========================
    // ✅ 댓글 취소 (원복)
    // =========================
    const cancelBtn = e.target.closest(".comment-cancel-btn");
    if (cancelBtn) {
        const card = cancelBtn.closest(".comment-card");
        if (!card) return;

        // 편집 UI 제거
        card.querySelector(".comment-edit-textarea")?.remove();
        card.querySelector(".comment-save-btn")?.remove();
        card.querySelector(".comment-cancel-btn")?.remove();

        // 원래 텍스트 다시 표시
        const contentEl = card.querySelector(".comment-content");
        if (contentEl) contentEl.style.display = "";

        // 수정/삭제 버튼 다시 표시
        const modifyBtnInCard = card.querySelector(".comment-modify-btn");
        const deleteBtnInCard = card.querySelector(".comment-delete-btn");
        if (modifyBtnInCard) modifyBtnInCard.style.display = "";
        if (deleteBtnInCard) deleteBtnInCard.style.display = "";

        card.dataset.editing = "false";
        return;
    }
});

const logOutButton = document.getElementById('logout-btn');

if(logOutButton){
    logOutButton.addEventListener('click', event => {
        function success(){
            alert("로그아웃됐습니다..")
            location.replace("/login");
        }
        function fail(){
            alert("로그아웃 실패했습니다.");
            location.replace("/login");
        }
        httpRequest("DELETE", "/api/token", null, success, fail);
    });
}


// =========================
// ✅ 좋아요 (페이지 리로드 방식)
// =========================
const likeButton = document.getElementById("like-btn");

if (likeButton) {
    likeButton.addEventListener("click", () => {
        const articleId = document.getElementById("article-id")?.value;
        if (!articleId) return;

        const liked = likeButton.dataset.liked === "true";
        const url = "/api/articles/" + articleId + "/like";

        function success() {
            location.replace("/articles/" + articleId);
        }

        function fail() {
            alert("좋아요 처리 실패했습니다.");
            location.replace("/articles/" + articleId);
        }

        httpRequest(liked ? "DELETE" : "POST", url, null, success, fail);
    });
}