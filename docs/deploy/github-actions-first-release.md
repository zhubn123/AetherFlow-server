# GitHub Actions 首发部署手册

## 目标

- `main`：部署基线分支，推送后自动发布
- `develop`：日常开发分支，不自动发布

## 当前仓库状态

- `AetherFlow-server` 已包含 `.github/workflows/deploy-server.yml`
- `AetherFlow-web` 已包含 `.github/workflows/deploy-web.yml`
- 两个 workflow 都仅在 `push main` 触发

## GitHub Secrets（两个仓库都要配置）

进入仓库：`Settings -> Secrets and variables -> Actions -> New repository secret`

- `SERVER_HOST`：`ssh.beldyl.ink`
- `SERVER_USER`：`berlin`
- `SERVER_PORT`：`22`
- `SERVER_SSH_KEY`：部署私钥内容（建议单独部署密钥）

## 服务器目录准备（只做一次）

```bash
sudo mkdir -p /srv/aetherflow/server/releases /srv/aetherflow/server/logs
sudo mkdir -p /srv/aetherflow/web/releases /srv/aetherflow/web/shared
sudo chown -R root:root /srv/aetherflow
```

## Nginx 配置模板

保存到 `/etc/nginx/conf.d/beldyl.ink.conf`：
mkdir -p /srv/aetherflow/web

cat >/etc/nginx/conf.d/aetherflow.conf <<'EOF'
server {
listen 80;
server_name beldyl.ink www.beldyl.ink;

    root /srv/aetherflow/web;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
EOF

nginx -t
systemctl reload nginx

```nginx
server {
    listen 80;
    server_name beldyl.ink www.beldyl.ink;

    root /srv/aetherflow/web/current;
    index index.html;

    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

校验并重载：

```bash
sudo nginx -t
sudo systemctl reload nginx
```

## 首次发布流程

1. 在两个仓库分别发起 `develop -> main` 的 PR
2. 合并 PR 到 `main`
3. 观察 `Actions` 页面：
   - `Deploy Server`
   - `Deploy Web`
4. 部署后验证：
   - 前端：`http://beldyl.ink`
   - 接口：`http://beldyl.ink/api/...`
   - 后端日志：`/srv/aetherflow/server/logs/app.log`

## 回滚（后端）

```bash
cd /srv/aetherflow/server/releases
ls -lt
ln -sfn /srv/aetherflow/server/releases/app-<OLDER_SHA>.jar /srv/aetherflow/server/current.jar
pkill -f "/srv/aetherflow/server/current.jar" || true
nohup java -jar /srv/aetherflow/server/current.jar --spring.profiles.active=prod > /srv/aetherflow/server/logs/app.log 2>&1 &
```

## 回滚（前端）

```bash
cd /srv/aetherflow/web/releases
ls -lt
ln -sfn /srv/aetherflow/web/releases/<OLDER_SHA>/dist /srv/aetherflow/web/current
sudo systemctl reload nginx
```
