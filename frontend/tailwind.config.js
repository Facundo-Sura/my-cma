/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        mycma: {
          gray: '#B0B0B0',
          carmine: '#960018',
          'carmine-light': '#C41E3A',
          navy: '#003366',
          'navy-light': '#0055A4',
          dark: '#1A1A2E',
        },
      },
    },
  },
  plugins: [],
};
