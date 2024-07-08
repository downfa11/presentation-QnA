document.addEventListener("DOMContentLoaded", function () {
    const menu = document.querySelectorAll(".menu-link");
    const menuToggle = document.querySelectorAll(".menu");


    menuToggle.forEach((toggle) => {
        toggle.addEventListener("click", function () {
            const next = toggle.nextElementSibling;
            next.classList.toggle("active");
        });
    });

    const sidebar = document.getElementById("sidebar");
    const content = document.getElementById("content");
    const toggleMenu = document.querySelector("[data-icon='menu']");

    toggleMenu.addEventListener("click", function () {
        sidebar.classList.toggle("active");
        content.classList.toggle("active");
    });

    async function fetchData() {
        try {
            const response = await fetch('/rooms/roomId/comments');
            if (!response.ok) {
                throw new Error('네트워크 응답이 정상적이지 않습니다');
            }
            const data = await response.json();
            displayMessages(data.commentDtos);
        } catch (error) {
            console.error('데이터를 가져오는 중 오류 발생:', error);
        }
    }

    function displayMessages(commentDtos) {
        const chatBox = document.querySelector('.chat-box');

        chatBox.innerHTML = '';

        commentDtos.forEach(comment => {
            const msgClass = comment.commentUserId === userId ? 'msg me' : 'msg';
            const html = `
                <div class="${msgClass}">
                    <div class="chat">
                        <div class="profile">
                            <span class="username">${comment.commentUserId}</span>
                            <span class="time">${comment.time}</span>
                        </div>
                        <p>${comment.contents}</p>
                    </div>
                </div>
            `;
            chatBox.innerHTML += html;
        });
    }

    document.addEventListener('DOMContentLoaded', () => {
        fetchData();
    });

});
