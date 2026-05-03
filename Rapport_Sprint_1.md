% Rapport de sprint 1
% Projet Emotionia
% 25 mars 2026

# 1. Contexte du sprint

Le projet **Emotionia** vise a creer une application Android capable d'afficher un flux camera et de presenter, a terme, une analyse d'emotions assistee par IA directement sur l'appareil.

Ce premier sprint avait pour objectif de poser une base technique stable et de produire une premiere experience utilisateur fonctionnelle. Le travail realise s'est donc concentre sur :

- l'initialisation du projet Android ;
- la mise en place d'une architecture claire pour l'evolution du produit ;
- l'integration du flux camera avec **CameraX** ;
- la creation d'une interface Compose inspiree d'un tableau de bord d'analyse emotionnelle ;
- la gestion d'un etat UI simule afin de valider le parcours utilisateur avant l'integration du moteur IA.

# 2. Travaux realises

## 2.1 Mise en place de l'environnement

Les elements suivants ont ete configures pendant le sprint :

- creation du projet Android en **Kotlin** ;
- configuration Gradle en **Kotlin DSL** ;
- activation de **Jetpack Compose** pour l'interface utilisateur ;
- organisation du projet selon une logique **MVVM** ;
- definition des dependances dans `gradle/libs.versions.toml`.

Le projet cible actuellement :

| Element | Valeur |
| --- | --- |
| Langage | Kotlin |
| UI | Jetpack Compose Material 3 |
| Architecture | MVVM |
| Min SDK | 24 |
| Target SDK | 36 |
| Compile SDK | 36 |

## 2.2 Architecture applicative

La structure logicielle du projet a ete clarifiee des le sprint 1 afin de faciliter les evolutions suivantes.

```text
com.mobile.emotion_ia
|- MainActivity
|- data
|  |- EmotionRepository
|  `- model/EmotionData
`- ui
   |- emotion_screen
   |  |- EmotionScreen
   |  `- EmotionViewModel
   `- theme
```

Les responsabilites sont separees de la facon suivante :

- **View** : `EmotionScreen.kt` affiche l'interface Compose ;
- **ViewModel** : `EmotionViewModel.kt` porte l'etat de l'ecran via `MutableStateFlow` ;
- **Model** : `EmotionData.kt` centralise les donnees necessaires a l'affichage ;
- **Repository** : `EmotionRepository.kt` prepare la couche d'acces aux donnees reelles.

Cette base permet d'integrer plus tard la camera, le traitement IA et la persistence sans remettre en cause la structure generale.

## 2.3 Integration de la camera

Une premiere integration de **CameraX** a ete realisee dans l'application.

Les points livres sont :

- declaration de la permission `android.permission.CAMERA` dans le manifeste ;
- verification de la permission a l'execution ;
- demande de permission depuis l'ecran principal si necessaire ;
- integration d'un composant `PreviewView` dans Compose via `AndroidView` ;
- liaison du flux avec `ProcessCameraProvider` ;
- utilisation de la **camera frontale** par defaut.

Cette integration permet deja d'afficher un apercu camera dans l'application, ce qui valide le socle technique pour la suite.

## 2.4 Interface utilisateur

Un premier ecran complet a ete implemente avec Jetpack Compose. Il reprend les principaux blocs fonctionnels attendus pour une application d'analyse emotionnelle :

- un titre d'application ;
- une carte d'apercu camera ;
- des barres de progression pour plusieurs emotions ;
- une carte d'etat indiquant le statut courant, le score, l'etat du modele et celui de la camera ;
- une rangee d'actions rapides (email, partage, camera) ;
- une carte de statistiques de session.

Les emotions affichees sont :

- neutral ;
- sad ;
- happy ;
- angry ;
- surprised.

Les animations de progression ont egalement ete ajoutees pour rendre l'affichage plus lisible et plus proche d'un produit final.

## 2.5 Gestion de l'etat et donnees de demonstration

Afin de valider rapidement l'experience utilisateur, l'application fonctionne actuellement avec des **donnees simulees** exposees par le `ViewModel`.

Le modele `EmotionData` contient notamment :

- les scores d'emotions ;
- un message de statut ;
- un score de confiance ;
- l'etat de chargement des modeles ;
- l'etat d'activation de la camera ;
- des statistiques de session.

Le `ViewModel` fournit egalement une action `resetStats()` qui reinitialise les statistiques affichees a l'ecran. Ce point a permis de tester le cycle complet : evenement utilisateur, mise a jour d'etat et rerendu Compose.

# 3. Resultats obtenus

Au terme du sprint 1, le projet dispose d'un premier socle fonctionnel coherent.

| Fonctionnalite | Etat |
| --- | --- |
| Structure Android / Gradle | Terminee |
| Architecture MVVM | Terminee |
| Theme Compose et ecran principal | Termines |
| Apercu CameraX dans l'interface | Termine |
| Gestion de permission camera | Terminee |
| Etat UI via `StateFlow` | Termine |
| Statistiques de session simulees | Terminees |
| Inference emotionnelle reelle | Non demarree |
| Repository branche a une source reelle | Non termine |

Le sprint 1 valide donc la faisabilite de la partie interface et de la brique camera, tout en laissant l'integration IA comme objectif principal du sprint suivant.

# 4. Limites identifiees

Malgre l'avancement obtenu, plusieurs limites restent presentes en fin de sprint :

- les emotions affichees proviennent de valeurs simulees et non d'un modele IA ;
- `EmotionRepositoryImpl` est encore un squelette non implemente ;
- aucun traitement image frame par frame n'est encore execute ;
- les actions d'email et de partage ne sont pas connectees a une logique metier ;
- les statistiques ne sont pas persistees localement.

Ces limites sont normales a ce stade et correspondent au perimetre volontairement restreint du sprint 1.

# 5. Perspectives pour le sprint 2

Le sprint suivant pourra s'appuyer sur la base deja mise en place pour accelerer le developpement. Les priorites proposees sont :

1. integrer le pipeline d'analyse d'image en temps reel ;
2. connecter un modele d'IA ou un moteur de classification emotionnelle ;
3. remplacer les donnees simulees par des donnees issues du traitement reel ;
4. finaliser la couche repository ;
5. enrichir les actions utilisateur (partage, export, historique).

# 6. Conclusion

Le sprint 1 a permis de construire une base technique propre, exploitable et demonstrable pour **Emotionia**. L'application dispose deja :

- d'une structure Android moderne ;
- d'un ecran Compose complet ;
- d'un apercu camera operationnel ;
- d'une gestion d'etat reactive ;
- d'un cadre d'architecture pret pour l'integration IA.

Le projet est donc bien positionne pour entamer, au sprint 2, la partie la plus strategique : la reconnaissance emotionnelle reelle et son exploitation dans l'experience utilisateur.
