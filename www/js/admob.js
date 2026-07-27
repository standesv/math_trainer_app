/* AdMob - bannière en bas d'écran (admob-plus-cordova) */
(function () {
  'use strict';

  var BANNER_AD_UNIT_ID = 'ca-app-pub-3209259150498249/5742724335';

  // ID de test officiel Google : utilisé automatiquement en debug pour
  // ne pas polluer les statistiques et éviter une suspension de compte.
  var TEST_BANNER_AD_UNIT_ID = 'ca-app-pub-3940256099942544/6300978111';

  var banner = null;

  function isDebugBuild() {
    // cordova.platformId existe sur device ; BuildInfo n'est pas installé,
    // on se base donc sur l'absence de minification / hostname localhost.
    return location.protocol === 'http:' || location.hostname === 'localhost';
  }

  function showBanner() {
    if (typeof admob === 'undefined') {
      console.warn('[AdMob] plugin indisponible - bannière ignorée');
      return;
    }

    var adUnitId = isDebugBuild() ? TEST_BANNER_AD_UNIT_ID : BANNER_AD_UNIT_ID;

    admob
      .start()
      .then(function () {
        banner = new admob.BannerAd({
          adUnitId: adUnitId,
          position: 'bottom'
        });
        return banner.show();
      })
      .then(function () {
        // Réserve la place sous la barre de navigation pour ne rien masquer.
        document.body.classList.add('has-ad-banner');
        console.log('[AdMob] bannière affichée');
      })
      .catch(function (err) {
        console.error('[AdMob] erreur :', err);
      });
  }

  function hideBanner() {
    if (!banner) return;
    banner.hide().catch(function () {});
    document.body.classList.remove('has-ad-banner');
  }

  document.addEventListener('deviceready', showBanner, false);

  // Exposé pour un usage manuel éventuel (ex. version premium sans pub)
  window.MathTrainerAds = { show: showBanner, hide: hideBanner };
})();
