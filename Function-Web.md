# 🌐 Functional Web Module in Spring WebFlux

The **Functional Web Module** in Spring WebFlux offers an alternative to annotation-based controllers using **functional programming** concepts. It provides explicit control over routing and request handling.

---

## ✅ What is It?

A way to define web endpoints using:
- `RouterFunction`: Routes HTTP requests.
- `HandlerFunction`: Handles the logic.
- `ServerRequest` and `ServerResponse`: Represent request/response.

---

## 🧱 Core Components

| Component        | Description |
|------------------|-------------|
| `RouterFunction` | Maps requests to handlers (like a routing table) |
| `HandlerFunction`| Handles the request and returns a response |
| `ServerRequest`  | Functional version of HttpServletRequest |
| `ServerResponse` | Functional version of HttpServletResponse |

---

## 📘 Traditional (Annotated) vs Functional

### 🎯 Annotated Controller

```java
@RestController
@RequestMapping("/products")
public class ProductController {

    @GetMapping("/{id}")
    public Mono<Product> getProduct(@PathVariable String id) {
        return productService.getProductById(id);
    }
}
```

### Functional Style (Router + Handler)

###  Router Function

```
@Configuration
public class RouterConfig {

    @Bean
    public RouterFunction<ServerResponse> productRoutes(ProductHandler handler) {
        return RouterFunctions
            .route(RequestPredicates.GET("/products/{id}"), handler::getProductById);
    }
}
```
### 🛠 Handler Class
```
@Component
public class ProductHandler {

    public Mono<ServerResponse> getProductById(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<Product> productMono = productService.getProductById(id);
        return productMono.flatMap(product ->
            ServerResponse.ok().bodyValue(product)
        );
    }
}
```