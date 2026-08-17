# Theming Documentation: Vola Travel App (Android)

This document details the design system of the Vola Android application, including the color palette, typography, and the dynamic theming system.

## 1. Color System

Vola uses the **Material 3 (Material You)** design system, featuring a core palette that adapts to user preferences (Dark/Light mode) and destination-specific contexts.

### 1.1 Base Color Palette

The default "Vola" theme is characterized by warm, earthy tones that evoke a sense of travel and exploration.

#### Light Theme
| Token | Value | Description |
| :--- | :--- | :--- |
| **Primary** | `#7C580D` | Main brand color (Golden Brown). |
| **Primary Container** | `#FFDEAB` | Soft background for primary actions. |
| **Secondary** | `#6E5C3F` | Muted accent color. |
| **Background** | `#FFF8F3` | Warm off-white page background. |
| **Surface** | `#FFF8F3` | Surface color for cards and panels. |
| **Error** | `#BA1A1A` | Standard error red. |

#### Dark Theme
| Token | Value | Description |
| :--- | :--- | :--- |
| **Primary** | `#EFBF6D` | Brand color optimized for dark backgrounds. |
| **Primary Container** | `#5F4100` | Deep accent for dark mode. |
| **Secondary** | `#DBC3A1` | Light earthy accent. |
| **Background** | `#17130B` | Deep black/brown background. |
| **Surface** | `#17130B` | Main surface color in dark mode. |

### 1.2 Dynamic Theming (Material You)

A core feature of the Vola experience is **Contextual Dynamic Theming**. 

1.  **Seed Color Extraction**: The application uses the **Android Palette API** to extract a dominant "seed color" from the hero image of the currently focused city in the itinerary.
2.  **Color Scheme Generation**: This seed color is used to generate a full Material 3 `ColorScheme` (Light and Dark) on the fly.
3.  **Visual Style**: The app specifically uses `PaletteStyle.Expressive`, which prioritizes more vibrant and saturated variations of the seed color to create a high-impact, immersive UI.
4.  **Application**: All Material components (Floating Action Buttons, Top Bars, Navigation Bars, and Event Cards) automatically update to the new color scheme.

---

## 2. Typography

Vola uses a combination of two font families to balance readability with a distinct brand personality.

### 2.1 Font Families

- **Display Font: *Oregano***
    - **Source**: Google Fonts.
    - **Usage**: Used for Large, Medium, and Small Display styles (e.g., Trip Titles, prominent City Banners).
    - **Characteristic**: A casual, handwritten-style font that adds a personal, "travel journal" feel.
- **Body Font: *Roboto***
    - **Source**: Google Fonts (System Default).
    - **Usage**: Used for Headlines, Titles, Body text, and Labels.
    - **Characteristic**: Highly legible, clean, and modern.

### 2.2 Text Styles

| Style | Font Family | Size | Case | Usage |
| :--- | :--- | :--- | :--- | :--- |
| **Display Large** | Oregano | 57sp | Title Case | Main Trip Title on Home. |
| **Display Medium** | Oregano | 45sp | Title Case | Focused City Title in Itinerary. |
| **Headline Small** | Roboto | 24sp | Sentence Case | Card Titles. |
| **Body Large** | Roboto | 16sp | Sentence Case | Descriptions and primary text. |
| **Label Small** | Roboto | 11sp | Upper Case | Metadata (e.g., Dates, Airport Codes). |

---

## 3. Design Principles

- **Immersion**: The UI should reflect the destination. When viewing "Kyoto," the app should feel different than when viewing "Reykjavik."
- **Clarity**: Use distinct visual markers (connected lines, dots) for the chronological travel flow.
- **Responsiveness**: All components use Material 3 adaptive tokens to ensure they look excellent across different screen sizes and themes.
