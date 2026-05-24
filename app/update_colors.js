const fs = require('fs');
let content = fs.readFileSync('app/src/main/java/com/example/ui/GPACalculatorUI.kt', 'utf8');

if (!content.includes('import androidx.compose.foundation.isSystemInDarkTheme')) {
    content = content.replace('import androidx.compose.foundation.layout.*', 'import androidx.compose.foundation.layout.*\nimport androidx.compose.foundation.isSystemInDarkTheme');
}

const replacements = [
    [/Color\(0xFFF7F9FB\)/g, "if (isSystemInDarkTheme()) Color(0xFF111418) else Color(0xFFF7F9FB)"],
    [/Color\.White/g, "if (isSystemInDarkTheme()) Color(0xFF1E2329) else Color.White"],
    [/Color\(0xFF004A77\)/g, "if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77)"],
    [/Color\(0xFF041E49\)/g, "if (isSystemInDarkTheme()) Color(0xFFE1EBF5) else Color(0xFF041E49)"],
    [/Color\(0xFF001D33\)/g, "if (isSystemInDarkTheme()) Color(0xFFE1EBF5) else Color(0xFF001D33)"],
    [/Color\(0xFF44474E\)/g, "if (isSystemInDarkTheme()) Color(0xFFBEC6DC) else Color(0xFF44474E)"],
    [/Color\(0xFFE1E2E9\)/g, "if (isSystemInDarkTheme()) Color(0xFF44474E) else Color(0xFFE1E2E9)"],
    [/Color\(0xFFC2D6F6\)/g, "if (isSystemInDarkTheme()) Color(0xFF2E4057) else Color(0xFFC2D6F6)"],
    [/Color\(0xFFD3E3FD\)/g, "if (isSystemInDarkTheme()) Color(0xFF1C2B3E) else Color(0xFFD3E3FD)"],
    [/Color\(0xFFB02A37\)/g, "if (isSystemInDarkTheme()) Color(0xFFFFB4AB) else Color(0xFFB02A37)"],
    [/Color\(0xFF0F5132\)/g, "if (isSystemInDarkTheme()) Color(0xFF81C995) else Color(0xFF0F5132)"],
    [/Color\(0xFFF8D7DA\)/g, "if (isSystemInDarkTheme()) Color(0xFF93000A) else Color(0xFFF8D7DA)"],
    [/Color\(0xFFD1E7DD\)/g, "if (isSystemInDarkTheme()) Color(0xFF00391C) else Color(0xFFD1E7DD)"],
];

for (const [regex, replacement] of replacements) {
    content = content.replace(regex, replacement);
}

fs.writeFileSync('app/src/main/java/com/example/ui/GPACalculatorUI.kt', content);
console.log("Colors replaced for dark mode");
