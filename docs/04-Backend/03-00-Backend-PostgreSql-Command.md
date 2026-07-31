Installation PostgreSql:
	psql --version
	sudo apt update
	sudo apt install postgresql-client -y
	sudo systemctl status postgresql
	sudo apt update
	sudo apt install postgresql -y

Étape 1 — Se connecter au serveur PostgreSQL
	sudo -u postgres psql
Étape 2 — Lister les rôles existants
	\du
Étape 3 — Créer le rôle applicatif
	CREATE ROLE pcms_app
	WITH
	    LOGIN
	    PASSWORD 'PcmsDev2026!';
Étape 4 — Vérifier le rôle
	\du
Étape 5 — Créer la base de données
	CREATE DATABASE pcms
	OWNER pcms_app;
Étape 6 — Vérifier les bases
	\l
Étape 7 — Quitter PostgreSQL
	\q
Vérification finale
	psql -h localhost -U pcms_app -d pcms
