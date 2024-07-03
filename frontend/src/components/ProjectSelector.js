import React from 'react';
import './ProjectSelector.css';

const ProjectSelector = ({ projects, selectedProject, onSelectProject }) => {
  return (
    <div className="project-selector">
      <label htmlFor="project-select">Select Project:</label>
      <select
        id="project-select"
        value={selectedProject || ''}
        onChange={(e) => onSelectProject(e.target.value)}
      >
        {projects.map((project) => (
          <option key={project} value={project}>
            {project}
          </option>
        ))}
      </select>
    </div>
  );
};

export default ProjectSelector;
