document.addEventListener("DOMContentLoaded", function () {
    const menu = document.querySelectorAll(".menu-link");
    const menuToggle = document.querySelectorAll(".menu");

    menuToggle.forEach((toggle) => {
        toggle.addEventListener("click", function () {
            const next = toggle.nextElementSibling;
            next.classList.toggle("active");
        });
    });

    const content = document.getElementById("content");
    const toggleMenu = document.querySelector("[data-icon='menu']");

    toggleMenu.addEventListener("click", function () {
        content.classList.toggle("active");
    });

    async function fetchData() {

        const roomId = 'room';
        try {
            const response = await fetch(`/rooms/${roomId}/comments`);
            if (!response.ok) {
                throw new Error('네트워크 응답이 정상적이지 않습니다');
            }
            const data = await response.json();
            displayMessages(data.comments);
        } catch (error) {
            console.error('데이터를 가져오는 중 오류 발생:', error);
        }
    }

    function displayMessages(comments) {
        const chatBox = document.querySelector('.chat-box');
        const userId = "user1";
        chatBox.innerHTML = '';

        comments.forEach(comment => {
            const msgClass = comment.userId === userId ? 'msg me' : 'msg';
            console.log("내가 보낸건지 여부 : "+msgClass);
            const html = `
            <div class="${msgClass}">
                <div class="chat">
                    <div class="profile">
                        <span class="username">${comment.userId}</span>
                        <span class="time">${comment.date}</span>
                    </div>
                    <p>${comment.contents}</p>
                </div>
            </div>
        `;
            chatBox.innerHTML += html;
        });
    }

    const chatForm = document.querySelector('.chatbox-message');
    if (!chatForm) {
        console.error('Chat form element not found');
        return;
    }

    chatForm.addEventListener('submit', async function (event) {
        event.preventDefault();

        const inputField = document.getElementById('message-input');
        if (!inputField) {
            console.error('Message input element not found');
            return;
        }

        const message = inputField.value.trim();
        if (!message) {
            console.log("Empty message, not sending");
            return;
        }

        const roomId = 'room';

        try {
            const response = await fetch(`/rooms/${roomId}/comments`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ contents: message, userId: "user" })
            });

            if (response.ok) {
                inputField.value = '';
                fetchData();
            } else {
                console.error('Failed to post message:', response.statusText);
            }
        } catch (error) {
            console.error('Error posting message:', error);
        }
    });

    function pollData() {
        try {
            fetchData();
        } catch (error) {
            console.error('Error in pollData:', error);
        } finally {
            setTimeout(pollData, 3000);
        }
    }

    pollData();
});
