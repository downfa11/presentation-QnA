document.addEventListener("DOMContentLoaded", function () {
    let sortCriterion = "date";
    let sortOrder = "desc";
    const userId = sessionStorage.getItem('userId');

    fetch('/api/user-info')
        .then(response => response.json())
        .then(data => {
            sessionStorage.setItem('nickname', data.nickname);
            sessionStorage.setItem('thumbnailImage', data.thumbnailImage);
            sessionStorage.setItem('jwtToken', data.jwtToken);

            const nickname = sessionStorage.getItem('nickname');
            const thumbnailImage = sessionStorage.getItem('thumbnailImage');
            const jwtToken = sessionStorage.getItem('jwtToken');

            const profileImg = document.querySelector('.profile img');
            const defaultProfile = document.querySelector('.default-profile');


            if (jwtToken!="null" || !sessionStorage.getItem(userId)) {

                console.log("User is logged in. "+nickname +" (userId:"+userId+")");

                if (profileImg && thumbnailImage) {
                    profileImg.src = thumbnailImage;
                    profileImg.style.display = 'block';
                    defaultProfile.style.display = 'none';
                } else {
                    profileImg.style.display = 'none';
                    defaultProfile.style.display = 'block';
                }

                const nicknameSpan = document.querySelector('.content-data p span');
                if (nicknameSpan) {
                    nicknameSpan.textContent = userId;
                }
            }
            else {
                console.warn("User is not logged in.");
                window.location.href = '/';
            }
        })
        .catch(error => {
            console.error('Error fetching user info:', error);
        });

    const url = window.location.href;
    const lastSegment = url.substring(url.lastIndexOf('/') + 1);

    let roomId = lastSegment;
    const roomNameSpan = document.getElementById("room-name");
    roomNameSpan.textContent = roomId;

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

    function scrollToBottom() {
        const chatBox = document.querySelector('.chat-box');
        chatBox.scrollTop = chatBox.scrollHeight;
    }

    document.addEventListener('DOMContentLoaded', scrollToBottom);

    const qrButton = document.getElementById('qr-button');
    const qrCodeContainer = document.getElementById('qr-code-container');

    async function generateQRCode() {
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
                qrCodeContainer.innerHTML = `
                <img id="qr-code-img" src="${url}" alt="QR Code">
                <div id="qr-buttons">
                    <button id="qr-size-small">작게</button>
                    <button id="qr-size-medium">보통</button>
                    <button id="qr-size-large">크게</button>
                </div>
            `;
                qrCodeContainer.style.display = 'block';

                document.getElementById('qr-size-small').addEventListener('click', () => {
                    setQRCodeSize('small');
                });
                document.getElementById('qr-size-medium').addEventListener('click', () => {
                    setQRCodeSize('medium');
                });
                document.getElementById('qr-size-large').addEventListener('click', () => {
                    setQRCodeSize('large');
                });
            } else {
                console.error('Failed to generate QR code:', response.statusText);
            }
        } catch (error) {
            console.error('Error generating QR code:', error);
        }
    }

    function setQRCodeSize(size) {
        const img = document.getElementById('qr-code-img');

        switch (size) {
            case 'small':
                img.style.width = '100px';
                break;
            case 'medium':
                img.style.width = '300px';
                break;
            case 'large':
                img.style.width = '500px';
                break;
            default:
                img.style.width = '200px';
                break;
        }
    }

    function hideQRCode() {
        qrCodeContainer.innerHTML = '';
        qrCodeContainer.style.display = 'none';
    }

    qrButton.addEventListener('click', () => {
        if (qrCodeContainer.style.display === 'block') {
            hideQRCode();
        } else {
            generateQRCode();
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

    document.querySelectorAll('.sort-pagination a').forEach(link => {
        link.addEventListener('click', function(event) {
            event.preventDefault();
            sortCriterion = this.dataset.sort;
            sortOrder = this.dataset.order;

            console.log(this.dataset);
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

    async function checkRoomExistence(roomId) {
        try {
            const response = await fetch(`/find/rooms/${roomId}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                    // , 'Authorization': `Bearer ${jwtToken}`
                }
            });

            if (response.ok) {
                sessionStorage.setItem('roomId', roomId);
                window.location.href = `/rooms/${roomId}`;
            } else if (response.status === 404) {
                alert(`Room '${roomId}' does not exist.`);
            } else {
                throw new Error('Failed to fetch room. Check again Auth.');
            }

        } catch (error) {
            alert('Error checking room existence: ' + error.message);
        }
    }

    const searchIcon = document.getElementById('search-icon');
    searchIcon.addEventListener('click', function() {
        performSearch();
    });

    const searchInput = document.getElementById('search-input');
    searchInput.addEventListener('keypress', function(event) {
        if (event.key === 'Enter') {
            performSearch();
        }
    });

    function performSearch() {
        const roomId = searchInput.value.trim();
        if (roomId) {
            checkRoomExistence(roomId);
        } else {
            alert('검색어를 입력하세요.');
        }
    }


    function displayMessages(comments) {
        const chatBox = document.querySelector('.chat-box');
        chatBox.innerHTML = '';

        comments.forEach(comment => {
            const msgClass = comment.userId === userId ? 'msg me' : 'msg';
            const html = `
            <div class="${msgClass}" data-date="${comment.date}" data-likesCount="${comment.likesCount}" data-commentId="${comment.commentId}">
                <div class="chat">
                    <div class="profile">
                        <span class="username">${comment.userId}</span>
                        <span class="time">${formatTimestamp(comment.date)}</span>
                    </div>
                    <p>${comment.contents}</p>
                    <div class="buttons"> 
                        <button class="like-button">💓 ${comment.likesCount} </button>
                                ${comment.userId === userId ? `
                                    <button class="edit-button">수정</button>
                                    <button class="delete-button">삭제</button>
                                ` : ''}
                                
                                ${userId === roomId ? `
                                    <button class="done-button">완료</button> `: ''}
                    </div>
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

        document.querySelectorAll('.done-button').forEach(button => {
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
                body: JSON.stringify({ contents: message, userId: userId })
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
            setTimeout(pollData, 1500);
        }
    }

    fetchData(sortCriterion, sortOrder);
    pollData();
});
