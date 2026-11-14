enum SocialMedia: CaseIterable {
  case telegram
  case twitter
  case instagram
  case facebook
  case reddit
  case matrix
  case fosstodon
  case linkedin
  case universalMapsEmail
  case github
  case tiktok
  case threads
  case bluesky

  var link: String {
    switch self {
    case .telegram:
      return L("telegram_url")
    case .github:
      return "https://github.com/universalmaps/universalmaps/"
    case .linkedin:
      return "https://www.linkedin.com/company/organic-maps/"
    case .universalMapsEmail:
      return AppConstants.contactEmail
    case .matrix:
      return "https://matrix.to/#/#universalmaps:matrix.org"
    case .fosstodon:
      return "https://fosstodon.org/@universalmaps"
    case .facebook:
      return "https://facebook.com/UniversalMaps"
    case .twitter:
      return "https://twitter.com/UniversalMapsApp"
    case .instagram:
      return L("instagram_url")
    case .reddit:
      return "https://www.reddit.com/r/universalmaps/"
    case .tiktok:
      return "https://www.tiktok.com/@universalmaps"
    case .threads:
      return "https://www.threads.com/@universalmaps.app"
    case .bluesky:
      return "https://bsky.app/profile/universalmaps.bsky.social"
    }
  }

  var image: UIImage {
    switch self {
    case .telegram:
      return UIImage(named: "ic_social_media_telegram")!
    case .github:
      return UIImage(named: "ic_social_media_github")!
    case .linkedin:
      return UIImage(named: "ic_social_media_linkedin")!
    case .universalMapsEmail:
      return UIImage(named: "ic_social_media_mail")!
    case .matrix:
      return UIImage(named: "ic_social_media_matrix")!
    case .fosstodon:
      return UIImage(named: "ic_social_media_fosstodon")!
    case .facebook:
      return UIImage(named: "ic_social_media_facebook")!
    case .twitter:
      return UIImage(named: "ic_social_media_x")!
    case .instagram:
      return UIImage(named: "ic_social_media_instagram")!
    case .reddit:
      return UIImage(named: "ic_social_media_reddit")!
    case .tiktok:
      return UIImage(named: "ic_social_media_tiktok")!
    case .threads:
      return UIImage(named: "ic_social_media_threads")!
    case .bluesky:
      return UIImage(named: "ic_social_media_bluesky")!
    }
  }
}
