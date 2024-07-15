document.addEventListener("DOMContentLoaded", function () {
    let sortCriterion = "date";
    let sortOrder = "desc";
    let roomId="room";
    let userId="user1";

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

    document.getElementById('qr-button').addEventListener('click', async function () {
        try {
            const response = await fetch(`/qrcode/${roomId}`, {
                method: 'GET',
                headers: {
                    'Accept': 'image/png'
                }
            });

            if (response.ok) {
                const blob = await response.blob();
                const url = URL.createObjectURL(blob);

                const qrCodeContainer = document.getElementById('qr-code-container');
                qrCodeContainer.innerHTML = `<img src="${url}" alt="QR Code">`;
            } else {
                console.error('Failed to generate QR code:', response.statusText);
            }
        } catch (error) {
            console.error('Error generating QR code:', error);
        }
    });

    function formatTimestamp(timestamp) {
        const year = timestamp.substring(0, 4);
        const month = timestamp.substring(4, 6);
        const day = timestamp.substring(6, 8);
        const hour = timestamp.substring(8, 10);
        const minute = timestamp.substring(10, 12);
        const second = timestamp.substring(12, 14);

        const formattedTimestamp = `${year}-${month}-${day}-${hour}:${minute}:${second}`;

        return formattedTimestamp;
    }

    async function fetchData(criteria, order) {
        try {
            const response = await fetch(`/rooms/${roomId}/comments`);
            if (!response.ok) {
                throw new Error('네트워크 응답이 정상적이지 않습니다');
            }
            const data = await response.json();
            const sortedComments = sortComments(data.comments, criteria, order);
            displayMessages(sortedComments);
        } catch (error) {
            console.error('데이터를 가져오는 중 오류 발생:', error);
        }
    }

    document.querySelectorAll('.breadcrumbs a').forEach(link => {
        link.addEventListener('click', function(event) {
            event.preventDefault();
            sortCriterion = this.dataset.sort;
            sortOrder = this.dataset.order;
            fetchData(sortCriterion, sortOrder);
        });
    });

    function sortComments(comments, criteria, order) {
        return comments.sort((a, b) => {
            const aValue = parseInt(a[criteria], 10);
            const bValue = parseInt(b[criteria], 10);

            if (order === 'asc') {
                return aValue - bValue;
            } else {
                return bValue - aValue;
            }
        });
    }

    async function handleLike(event) {
        const button = event.target.closest('.like-button');
        const commentElem = button.closest('.msg');
        const commentId = commentElem.dataset.commentid;

        try {
            const response = await fetch(`/rooms/${roomId}/comments/${commentId}/likes?userId=${userId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                fetchData(sortCriterion, sortOrder);
            } else {
                console.error('Failed to like comment:', response.statusText);
            }
        } catch (error) {
            console.error('Error liking comment:', error);
        }
    }

    async function handleEdit(event) {
        const commentElem = event.target.closest('.msg');
        const commentId = commentElem.dataset.commentId;
        const newContent = prompt('수정할 내용을 입력하세요:', commentElem.querySelector('p').innerText);

        if (newContent) {
            try {
                const response = await fetch(`/rooms/${roomId}/comments/${commentId}`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ contents: newContent })
                });

                if (response.ok) {
                    fetchData(sortCriterion, sortOrder);
                } else {
                    console.error('Failed to edit comment:', response.statusText);
                }
            } catch (error) {
                console.error('Error editing comment:', error);
            }
        }
    }

    async function handleDelete(event) {
        const commentId = event.target.closest('.msg').dataset.commentId;

        if (confirm('정말로 삭제하시겠습니까?')) {
            try {
                const response = await fetch(`/rooms/${roomId}/comments/${commentId}`, {
                    method: 'DELETE'
                });

                if (response.ok) {
                    fetchData(sortCriterion, sortOrder);
                } else {
                    console.error('Failed to delete comment:', response.statusText);
                }
            } catch (error) {
                console.error('Error deleting comment:', error);
            }
        }
    }

    function displayMessages(comments) {
        const chatBox = document.querySelector('.chat-box');
        const userId = "user1";
        chatBox.innerHTML = '';

        comments.forEach(comment => {
            const msgClass = comment.userId === userId ? 'msg me' : 'msg';
            const html = `
            <div class="${msgClass}" data-date="${comment.date}" data-likes="${comment.likesCount}" data-commentId="${comment.commentId}">
                <div class="chat">
                    <div class="profile">
                        <span class="username">${comment.userId}</span>
                        <span class="time">${formatTimestamp(comment.date)}</span>
                    </div>
                    <p>${comment.contents}</p>
                    <button class="like-button">👍🏻 ${comment.likesCount}</button>
                                ${comment.userId === userId ? `
                                    <button class="edit-button">수정</button>
                                    <button class="delete-button">삭제</button>
                                ` : ''}
                </div>
            </div>
        `;
            chatBox.innerHTML += html;
        });

        document.querySelectorAll('.like-button').forEach(button => {
            button.addEventListener('click', handleLike);
        });

        document.querySelectorAll('.edit-button').forEach(button => {
            button.addEventListener('click', handleEdit);
        });

        document.querySelectorAll('.delete-button').forEach(button => {
            button.addEventListener('click', handleDelete);
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
                fetchData(sortCriterion, sortOrder);
            } else {
                console.error('Failed to post message:', response.statusText);
            }
        } catch (error) {
            console.error('Error posting message:', error);
        }
    });

    function pollData() {
        try {
            fetchData(sortCriterion, sortOrder);
        } catch (error) {
            console.error('Error in pollData:', error);
        } finally {
            setTimeout(pollData, 3000);
        }
    }

    fetchData(sortCriterion, sortOrder);
    pollData();
});
