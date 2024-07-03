import React, { useState, forwardRef, useImperativeHandle } from 'react';
import './SearchBar.css';


const SearchBar = forwardRef(({ onSearch }, ref) => {
 const [query, setQuery] = useState('');
 const [suggestions, setSuggestions] = useState([]);


 useImperativeHandle(ref, () => ({
   resetSearch: () => {
     setQuery('');
     setSuggestions([]);
   }
 }));


 const fetchSuggestions = async (searchQuery) => {
   if (!searchQuery) {
     setSuggestions([]);
     return;
   }
   try {
     const response = await fetch(`/api/autocomplete?term=${encodeURIComponent(searchQuery)}`);
     if (!response.ok) {
       throw new Error('Failed to fetch suggestions');
     }
     const data = await response.json();
     setSuggestions(data);
   } catch (error) {
     console.error('Error fetching suggestions:', error);
     setSuggestions([]);
   }
 };


 const handleChange = (e) => {
   const searchQuery = e.target.value;
   setQuery(searchQuery);
   fetchSuggestions(searchQuery);
 };


 const handleSuggestionClick = (suggestion) => {
   setQuery(suggestion);
   setSuggestions([]);
   onSearch(suggestion);
 };


 const handleSubmit = (e) => {
     e.preventDefault();
     onSearch(query);
   };


 const handleClear = () => {
     setQuery('');
     setSuggestions([]);
   };


return (
    <form onSubmit={handleSubmit} className="search-bar">
      <input
        type="text"
        value={query}
        onChange={handleChange}
        placeholder="Search for a node..."
        className="search-input"
      />
      {suggestions.length > 0 && (
        <ul className="suggestions-list">
          {suggestions.map((suggestion, index) => (
            <li
              key={index}
              onClick={() => handleSuggestionClick(suggestion)}
              className="suggestion-item"
            >
              {suggestion}
            </li>
          ))}
        </ul>
      )}
    </form>
  );
});

export default SearchBar;