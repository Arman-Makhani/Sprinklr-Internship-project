// Function to display the SVG content
function displaySvg(svgContent) {
    document.getElementById('svgContainer').innerHTML = svgContent;
}

// Event listener for the generate button
document.getElementById('generateButton').addEventListener('click', () => {
    const fileInput = document.getElementById('fileInput');
    if (!fileInput.files.length) {
        alert('Please select a file to upload');
        return;
    }

    const formData = new FormData();
    formData.append('file', fileInput.files[0]);

    // Fetch and display SVG
    fetch('http://localhost:8080/generate', { // Adjust the URL to your backend endpoint
        method: 'POST',
        body: formData
    })
    .then(response => response.text())
    .then(data => displaySvg(data))
    .catch(error => console.error('Error:', error));
});
