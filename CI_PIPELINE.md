# 🔄 Pipeline de CI (Integración Continua)

## ¿Qué es este pipeline?

Este pipeline de GitHub Actions se ejecuta automáticamente cada vez que:
- Haces `push` a las ramas `main` o `develop`
- Abres un Pull Request contra `main` o `develop`

## 🎯 ¿Qué hace el pipeline?

### 1. **Checkout del código**
- Descarga la última versión de tu código del repositorio

### 2. **Configuración del entorno**
- Instala Java 17 (Temurin)
- Configura Maven con cache para acelerar builds

### 3. **Ejecución de tests**
- Ejecuta `mvn clean test` con el perfil `test`
- Usa H2 database en memoria (más rápido que PostgreSQL)
- Si algún test falla, el pipeline se marca en **ROJO** ❌

### 4. **Build del proyecto**
- Ejecuta `mvn clean package -DskipTests`
- Compila el JAR final
- Si la compilación falla, el pipeline se marca en **ROJO** ❌

### 5. **Subida de artifacts**
- Guarda los resultados de tests (si fallan)
- Guarda el JAR compilado (si todo pasa)

## 🚨 ¿Qué pasa si falla?

### **Tests fallan:**
- Pipeline se marca en **ROJO** ❌
- Recibes notificación por email
- No puedes hacer merge si tienes branch protection
- Puedes descargar los reportes de error

### **Build falla:**
- Pipeline se marca en **ROJO** ❌
- Error de compilación visible en los logs
- No se genera el JAR

### **Todo pasa:**
- Pipeline se marca en **VERDE** ✅
- JAR se guarda como artifact
- Puedes hacer merge con confianza

## 📋 Cómo usar el pipeline

### 1. **Primer push:**
```bash
git add .
git commit -m "Add CI pipeline"
git push origin main
```

### 2. **Verificar el pipeline:**
- Ve a tu repositorio en GitHub
- Pestaña "Actions"
- Verás el pipeline ejecutándose en tiempo real

### 3. **Si falla:**
- Revisa los logs en la pestaña "Actions"
- Descarga los artifacts para ver detalles
- Arregla el problema localmente
- Haz push nuevamente

## 🔧 Configuración local

Para probar localmente antes de hacer push:

```bash
# Ejecutar tests
mvn clean test -Dspring.profiles.active=test

# Build completo
mvn clean package -DskipTests
```

## 📊 Beneficios del CI

### ✅ **Calidad del código**
- Detecta problemas antes de que lleguen a producción
- Asegura que el código compila y funciona

### ✅ **Confianza**
- Puedes mergear con seguridad
- Sabes que `main` siempre funciona

### ✅ **Automatización**
- No necesitas probar manualmente
- Se ejecuta automáticamente en cada push

### ✅ **Base para CD**
- Cuando quieras desplegar, ya tienes la mitad del trabajo
- Solo agregas pasos de despliegue

## 🎯 Próximos pasos

1. **Sube el código a GitHub**
2. **Verifica que el pipeline pase** ✅
3. **Configura branch protection** (opcional)
4. **Agrega más tests** según necesites
5. **Cuando estés listo, agrega CD** (despliegue automático)

## 🆘 Troubleshooting

### Error: "No tests found"
- Asegúrate de tener tests en `src/test/java/`
- Verifica que las clases de test terminen en `Test.java`

### Error: "Build failed"
- Revisa que todas las dependencias estén en `pom.xml`
- Verifica que no haya errores de sintaxis

### Error: "Context failed to load"
- Revisa la configuración en `application-test.yml`
- Verifica que las dependencias de Spring Boot estén correctas 