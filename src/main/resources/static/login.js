document.addEventListener("DOMContentLoaded", function () {

    fetch('/api/user-info')
        .then(response => response.json())
        .then(data => {
            sessionStorage.setItem('nickname', data.nickname);
            sessionStorage.setItem('thumbnailImage', data.thumbnailImage);
            sessionStorage.setItem('userId', data.userId);

            const nickname = sessionStorage.getItem('nickname');
            const thumbnailImage = sessionStorage.getItem('thumbnailImage');

            const createRoomButton = document.getElementById('create-room-button');
            const joinRoomButton = document.getElementById('join-room-button');
            const kakaoLoginLink = document.getElementById('kakao-login');
            const kakaoLogoutLink = document.getElementById('logout-button');

            const profileImg = document.querySelector('.profile img');
            const defaultProfile = document.querySelector('.default-profile');


            if (nickname!="null") {

                if (!sessionStorage.getItem('username')) {
                    const tempId = prompt('Enter your nickname:');
                    const encodedUserName = encodeURIComponent(tempId);
                    sessionStorage.setItem('username', encodedUserName);

                }

                const redirectUrl = sessionStorage.getItem('redirectUrl');
                if (redirectUrl) {
                    window.location.href = redirectUrl;
                    sessionStorage.removeItem('redirectUrl');
                    return;
                }

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
                    nicknameSpan.textContent = nickname;
                }

                if (createRoomButton) createRoomButton.classList.remove('hidden');
                if (joinRoomButton) joinRoomButton.classList.remove('hidden');
                if (kakaoLoginLink) kakaoLoginLink.style.display = 'none';
                if (kakaoLogoutLink) kakaoLogoutLink.style.display='block';

                createRoomButton.addEventListener('click', async function() {
                    try {
                        const userId = sessionStorage.getItem('userId');
                        const response = await fetch(`/rooms`, {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/json',
                                credentials: 'include'
                            }
                        });

                        if (response.ok) {
                            window.location.href = `/rooms/${userId}`;
                        } else {
                            alert('Failed to create room.');
                        }
                    } catch (error) {
                        alert('Error creating room: ' + error.message);
                    }
                });

                joinRoomButton.addEventListener('click', function() {
                    const roomId = prompt('Enter Room ID to join:');
                    if (roomId) {
                        checkRoomExistence(roomId);
                    }
                });
            }
            else {
                console.warn("User is not logged in.");
                if (createRoomButton) createRoomButton.classList.add('hidden');
                if (joinRoomButton) joinRoomButton.classList.add('hidden');
                if (kakaoLoginLink) kakaoLoginLink.style.display = 'block';
                if (kakaoLogoutLink) kakaoLogoutLink.style.display ='none';
                profileImg.style.display = 'none';
                defaultProfile.style.display = 'block';
            }
        })
        .catch(error => {
            console.error('Error fetching user info:', error);
        });


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

    async function checkRoomExistence(roomId) {
        try {
            const encodedRoomId = encodeURIComponent(roomId);
            const response = await fetch(`/find/rooms/${roomId}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    credentials: 'include'
                }
            });

                if (response.ok) {
                    sessionStorage.setItem('roomId', roomId);
                    window.location.href = `/rooms/${encodedRoomId}`;
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

});
