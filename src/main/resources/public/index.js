document.addEventListener("DOMContentLoaded", function () {
    async function fetchData(url, options = {}) {
        try {
            const response = await fetch("http://localhost:8080" + url, options);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const data = await response.text();
            document.getElementById("result").textContent = data;
            return data;
        } catch (error) {
            document.getElementById("result").textContent = `Error: ${error.message}`;
        }
    }

    function addPlayer() {
        const playerId = document.getElementById("playerId").value;
        if (!playerId) {
            const newPlayerId = fetchData(`/auto-create-player`, { method: 'POST' });
            newPlayerId.then((id) => {
                document.getElementById("playerId").value = id;
            });
        } else {
            fetchData(`/turn-on-pvp?id=${playerId}`, { method: 'POST' });
        }

        document.getElementById("pvpState").innerText = "ON";

        var button = document.getElementById("pvp-button");
        button.innerText = "Turn off PvP"
        button.onclick = removePlayer;
    };

    window.addPlayer = addPlayer;

    window.removePlayer = function () {
        const playerId = document.getElementById("playerId").value;
        if (!playerId) {
            alert("Please enter a Player ID.");
            return;
        }

        document.getElementById("pvpState").innerText = "OFF";

        var button = document.getElementById("pvp-button");
        button.innerText = "Turn on PvP"
        button.onclick = addPlayer;

        fetchData(`/turn-off-pvp?id=${playerId}`, { method: 'POST' });
    };

    window.getPlayer = function () {
        const playerId = document.getElementById("playerId").value;
        if (!playerId) {
            alert("Please enter a Player ID.");
            return;
        }

        fetchData(`/me?id=${playerId}`);
    };

    window.getMatch = function () {
        const playerId = document.getElementById("playerId").value;
        if (!playerId) {
            alert("Please enter a Player ID.");
            return;
        }

        fetchData(`/match?id=${playerId}`);
    };

    window.startMatches = function () {
        fetchData(`/start-matches`, { method: 'POST' });
    }

    window.getRank = function () {
        fetchData(`/rank`);
    };

    addPlayer();
});
