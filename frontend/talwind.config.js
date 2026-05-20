/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                "primary-fixed": "#a0f3d4",
                "background": "#fcf9f3",
                "tertiary-container": "#3b6d11",
                "on-surface": "#1c1c19",
                "surface-container-high": "#ebe8e2",
                "on-error": "#ffffff",
                "outline": "#6f7a74",
                "secondary": "#885200",
                "surface-container-low": "#f6f3ee",
                "error-container": "#ffdad6",
                "primary-container": "#0f6e56",
                "on-primary-fixed": "#002117",
                "error": "#ba1a1a",
                "surface-bright": "#fcf9f3",
                "outline-variant": "#bec9c3",
                "surface-container": "#f1ede8",
                "on-background": "#1c1c19",
                "secondary-container": "#fdad4e",
                "on-primary": "#ffffff",
                "on-secondary": "#ffffff",
                "on-error-container": "#93000a",
                "surface": "#fcf9f3",
                "surface-container-lowest": "#ffffff",
                "on-surface-variant": "#3f4944",
                "primary": "#005440"
                // ... include aici toate culorile din scriptul tău HTML original
            },
            spacing: {
                "container-padding": "24px",
            },
            fontFamily: {
                "body-md": ["Inter", "sans-serif"],
                "headline-md": ["Plus Jakarta Sans", "sans-serif"],
                "headline-sm": ["Plus Jakarta Sans", "sans-serif"],
                "label-md": ["Inter", "sans-serif"],
            },
        },
    },
    plugins: [],
}