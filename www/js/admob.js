// Configuration AdMob
const ADMOB_CONFIG = {
  APP_ID: 'ca-app-pub-3209259150498249~7379927993',
  BANNER_ID: 'ca-app-pub-3209259150498249/5742724335'
};

// Initialiser AdMob
function initAdMob() {
  if (typeof admob === 'undefined') {
    console.warn('AdMob plugin not available');
    return;
  }

  // Configuration initiale
  admob.setAppMuted(false);
  admob.setAppVolume(1);

  // Afficher la bannière pub
  showBannerAd();
}

// Afficher la bannière pub
function showBannerAd() {
  if (typeof admob === 'undefined') return;

  const bannerConfig = {
    id: ADMOB_CONFIG.BANNER_ID,
    isTesting: false, // Changer à true pour les tests
    autoShow: true
  };

  admob.banner.config(bannerConfig);

  admob.banner.prepare()
    .then(function() {
      return admob.banner.show();
    })
    .then(function() {
      console.log('Bannière AdMob affichée');
    })
    .catch(function(err) {
      console.error('Erreur AdMob:', err);
    });
}

// Masquer la bannière pub
function hideBannerAd() {
  if (typeof admob !== 'undefined') {
    admob.banner.hide();
  }
}

// Initialiser lors du chargement de l'app
document.addEventListener('DOMContentLoaded', function() {
  // Attendre que Cordova soit prêt
  if (document.readyState === 'loading') {
    document.addEventListener('deviceready', initAdMob);
  } else {
    // Cordova est déjà chargé
    setTimeout(initAdMob, 500);
  }
});

// Fallback pour les tests en web
if (!window.cordova) {
  console.log('App web - AdMob simulé');
}
