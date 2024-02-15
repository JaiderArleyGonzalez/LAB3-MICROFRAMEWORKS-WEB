let cache = new Map();
document.addEventListener("DOMContentLoaded", function() {
    var posterImg = document.getElementById("posterImg");
    var noPosterMsg = document.getElementById("noPoster");
    var posterContainer = document.querySelector(".poster-container");

    posterImg.onload = function() {
        posterContainer.style.display = "block";
        noPosterMsg.style.display = "none";
    };

    posterImg.onerror = function() {
        posterContainer.style.display = "none";
        noPosterMsg.style.display = "block";
    };
});
function loadGetMsg() {
    let titleVar = document.getElementById("title").value;
    let typeVar = document.getElementById("type").value;
    let yearVar = document.getElementById("year").value;
    let plotVar = document.getElementById("plot").value;
    let url = "http://www.omdbapi.com/?apikey=1892fc8d&t=" + titleVar;
    if (typeVar) { url += "&type=" + typeVar; }
    if (yearVar) { url += "&y=" + yearVar; }
    if (plotVar) { url += "&plot=" + plotVar; }
    if (cache.has(url)) {
        let data = cache.get(url);
        updateUI(data.html);
        updatePoster(data.poster);
    } else {
        const xhttp = new XMLHttpRequest();
        xhttp.onload = function() {
            let movieData = JSON.parse(this.responseText);
            if (movieData["Response"] === "True") {
                let formattedData = formatMovieDetails(movieData);
                updateUI(formattedData.html);
                updatePoster(movieData["Poster"]);
                cache.set(url, { html: formattedData.html, poster: movieData["Poster"] });
            } else {
                let errorMessage = "Sorry, the movie you searched for could not be found. Please try another one.";
                updateUI(errorMessage);
                updatePoster("N/A");
            }
        }
        xhttp.open("GET", url);
        xhttp.send();
    }
}
function updateUI(data) {
    document.querySelector(".movie-info").innerHTML = data;
}
function updatePoster(posterUrl) {
    let posterImg = document.getElementById("posterImg");
    let noPoster = document.getElementById("noPoster");
    if (posterUrl !== "N/A") {
        posterImg.src = posterUrl;
        noPoster.style.display = "none";
        posterImg.style.display = "block";
    } else {
        noPoster.style.display = "block";
        posterImg.style.display = "none";
    }
}
function formatMovieDetails(movie) {
    let html = "";
    html += "<div class='movie-info'>";
    html += "<h2>Movie Details</h2>";
    for (let key in movie) {
        if (movie[key] !== "N/A" && key !== "Poster" && key !== "Response" && key !== "imdbID") {
            if (key === "Ratings") {
                html += "<p><strong>Ratings:</strong></p>";
                html += "<ul>";
                for (let i = 0; i < movie[key].length; i++) {
                    html += "<li>" + movie[key][i].Source + ": " + movie[key][i].Value + "</li>";
                }
                html += "</ul>";
            } else {
                html += "<p><strong>" + key + ":</strong> " + movie[key] + "</p>";
            }
        }
    }
    html += "</div>";
    return { html: html };
}