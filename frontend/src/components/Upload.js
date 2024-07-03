import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const Upload = () => {
  const [file, setFile] = useState(null);
  const [message, setMessage] = useState('');
  const [fileName, setFileName] = useState('');
  const navigate = useNavigate();

  const handleFileChange = (event) => {
    const selectedFile = event.target.files[0];
    if (selectedFile && (selectedFile.name.endsWith('.log') || selectedFile.name.endsWith('.txt'))) {
      setFile(selectedFile);
      setFileName(selectedFile.name);
      setMessage('');
    } else {
      setFile(null);
      setFileName('');
      setMessage('Please select a .log or .txt file.');
    }
  };

 const validateGradleStructure = (content) => {
   const lines = content.split('\n');
   let inDependencyBlock = false;
   let currentIndentation = -1;
   let validLines = 0;

   for (const line of lines) {
     const trimmedLine = line.trim();

     // Check for the start of a new project or configuration
     if (trimmedLine.startsWith('------------------------------------------------------------')) {
       inDependencyBlock = false;
       currentIndentation = -1;
       continue;
     }

     // Check for the start of a dependency block
     if (trimmedLine.endsWith('- Compile classpath for source set \'main\'.') ||
         trimmedLine.endsWith('- Runtime classpath of source set \'main\'.') ||
         trimmedLine.startsWith('api - API dependencies for source set \'main\'.') ||
         trimmedLine.startsWith('compileClasspath - Compile classpath for source set \'main\'.')) {
       inDependencyBlock = true;
       currentIndentation = -1;
       continue;
     }

     if (inDependencyBlock) {
       if (trimmedLine.startsWith('+---') || trimmedLine.startsWith('\\---')) {
         const indentation = line.search(/\S/);
         if (indentation > currentIndentation || currentIndentation === -1) {
           currentIndentation = indentation;
           validLines++;
         } else if (indentation < currentIndentation) {
           currentIndentation = indentation;
         } else {
           // Same level indentation is allowed for siblings
         }
       } else if (trimmedLine === 'No dependencies') {
         validLines++;
       } else if (trimmedLine !== '') {
         // Any other non-empty line in a dependency block that doesn't start with +--- or \--- is invalid
         inDependencyBlock = false;
       }
     }
   }

   return validLines > 0;
 };

 const handleSubmit = async (event) => {
   event.preventDefault();
   if (!file) {
     setMessage('Please select a file.');
     return;
   }

   const reader = new FileReader();
   reader.onload = async (e) => {
     const content = e.target.result;
     if (!validateGradleStructure(content)) {
       setMessage('The file does not contain a valid Gradle dependency structure. Please check and re-upload.');
       return;
     }

     const formData = new FormData();
     formData.append('file', file);
     try {
       const response = await fetch('/generate', {
         method: 'POST',
         body: formData,
       });
       if (!response.ok) {
         throw new Error('File upload failed');
       }
       const result = await response.text();
       setMessage('Graph generated successfully!');
       navigate('/result', { state: { graphs: result } });
     } catch (error) {
       setMessage('Error: ' + error.message);
     }
   };
   reader.readAsText(file);
 };

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h1 style={styles.title}>Dependency Graph Visualizer</h1>
        <p style={styles.subtitle}>Upload your file to generate a graph</p>
        <form onSubmit={handleSubmit} style={styles.form}>
          <button type="button" onClick={() => document.getElementById('fileInput').click()} style={styles.fileInput}>
            {fileName || 'Choose a .log or .txt file'}
          </button>
          <input
            id="fileInput"
            type="file"
            onChange={handleFileChange}
            accept=".log,.txt"
            style={{ display: 'none' }}
          />
          <button type="submit" style={styles.button}>
            Generate Graph
          </button>
        </form>
        {message && <p style={styles.message}>{message}</p>}
      </div>
    </div>
  );
};

const styles = {
  container: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    minHeight: '100vh',
    backgroundColor: '#1C0C5B',
    width: '100vw',
    height: '100vh',
  },
  card: {
    backgroundColor: 'white',
    borderRadius: '8px',
    padding: '2rem',
    width: '100%',
    maxWidth: '400px',
    boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
  },
  title: {
    color: '#1C0C5B',
    fontSize: '24px',
    fontWeight: 'bold',
    marginBottom: '0.5rem',
    textAlign: 'center',
  },
  subtitle: {
    color: '#3D2C8D',
    fontSize: '16px',
    marginBottom: '1.5rem',
    textAlign: 'center',
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: '1rem',
  },
  fileInput: {
    backgroundColor: '#916BBF',
    color: 'white',
    padding: '0.75rem 1rem',
    borderRadius: '4px',
    cursor: 'pointer',
    textAlign: 'center',
    fontWeight: '500',
    width: '100%',
    border: 'none',
    fontSize: '16px',
  },
  button: {
    backgroundColor: '#3D2C8D',
    color: 'white',
    padding: '0.75rem 1rem',
    borderRadius: '4px',
    border: 'none',
    cursor: 'pointer',
    fontWeight: '500',
    width: '100%',
    fontSize: '16px',
  },
  message: {
    marginTop: '1rem',
    textAlign: 'center',
    color: '#C996CC',
  },
};

export default Upload;